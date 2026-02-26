package com.coze.loop.prompt;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.entity.ContentPart;
import com.coze.loop.entity.ExecuteParam;
import com.coze.loop.entity.ExecuteResult;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.entity.TokenUsage;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.exception.PromptException;
import com.coze.loop.http.HttpClient;
import com.coze.loop.internal.CozeLoopLogger;
import com.coze.loop.internal.JsonUtils;
import com.coze.loop.internal.ValidationUtils;
import com.coze.loop.spec.tracespec.SpanValues;
import com.coze.loop.stream.SSEDecoder;
import com.coze.loop.stream.SSEParser;
import com.coze.loop.stream.ServerSentEvent;
import com.coze.loop.stream.StreamReader;
import com.coze.loop.trace.CozeLoopSpan;

import okhttp3.Response;
import okhttp3.ResponseBody;

/** Provider for prompt operations: fetch, cache, and format. */
public class PromptProvider {
  private static final Logger logger = CozeLoopLogger.getLogger(PromptProvider.class);

  private final HttpClient httpClient;
  private final String promptEndpoint;
  private final String executeEndpoint;
  private final String executeStreamingEndpoint;
  private final String workspaceId;
  private final PromptCache cache;
  private final PromptFormatter formatter;
  private CozeLoopClient client;

  // Map cacheKey to GetPromptParam for fetching from server
  private final Map<String, GetPromptParam> paramMap = new ConcurrentHashMap<>();
  // Singleflight map for preventing duplicate requests
  private final Map<String, CompletableFuture<Prompt>> singleflightMap = new ConcurrentHashMap<>();

  public PromptProvider(
      HttpClient httpClient,
      String promptEndpoint,
      String workspaceId,
      PromptCache.PromptCacheConfig cacheConfig) {
    this(httpClient, promptEndpoint, null, null, workspaceId, cacheConfig);
  }

  public PromptProvider(
      HttpClient httpClient,
      String promptEndpoint,
      String executeEndpoint,
      String executeStreamingEndpoint,
      String workspaceId,
      PromptCache.PromptCacheConfig cacheConfig) {
    this.httpClient = httpClient;
    this.promptEndpoint = promptEndpoint;
    this.executeEndpoint = executeEndpoint;
    this.executeStreamingEndpoint = executeStreamingEndpoint;
    this.workspaceId = workspaceId;
    this.formatter = new PromptFormatter();

    // Initialize cache with this provider as the loader
    this.cache = new PromptCache(cacheConfig, this::fetchPromptFromServer);
  }

  /**
   * Set the client for tracing.
   *
   * @param client the client
   */
  public void setClient(CozeLoopClient client) {
    this.client = client;
  }

  /**
   * Get a prompt (with caching).
   *
   * @param param the parameters for getting prompt
   * @return prompt
   */
  public Prompt getPrompt(GetPromptParam param) {
    ValidationUtils.requireNonEmpty(param.getPromptKey(), "promptKey");

    CozeLoopSpan span = null;
    if (client != null) {
      span = client.startSpan("PromptHub", "prompt_hub", SpanValues.V_SCENE_PROMPT_HUB);
    }

    try {
      String cacheKey = buildCacheKey(param);

      // Store param mapping for fetchPromptFromServer to use
      paramMap.put(cacheKey, param);

      Prompt prompt = cache.getSync(cacheKey);
      if (prompt == null) {
        throw new PromptException(
            ErrorCode.PROMPT_NOT_FOUND,
            "Failed to get prompt: " + param.getPromptKey() + ". Cache returned null.");
      }

      if (span != null) {
        span.setTag("prompt_key", param.getPromptKey());
        span.setTag("prompt_version", prompt.getVersion());

        Map<String, Object> input = new HashMap<>();
        input.put("prompt_key", param.getPromptKey());
        input.put("prompt_version", param.getVersion());
        input.put("prompt_label", param.getLabel());
        span.setInput(JsonUtils.toJson(input));

        span.setOutput(JsonUtils.toJson(prompt));
      }

      return prompt;
    } catch (PromptException e) {
      if (span != null) {
        span.setError(e);
      }
      throw e;
    } catch (Exception e) {
      if (span != null) {
        span.setError(e);
      }
      throw new PromptException(
          ErrorCode.PROMPT_NOT_FOUND, "Failed to get prompt: " + param.getPromptKey(), e);
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  /**
   * Format prompt with variables.
   *
   * @param prompt the prompt
   * @param variables the variables to substitute
   * @return formatted messages
   */
  public List<Message> formatPrompt(Prompt prompt, Map<String, Object> variables) {
    ValidationUtils.requireNonNull(prompt, "prompt");

    if (variables == null) {
      variables = new HashMap<>();
    }

    CozeLoopSpan span = null;
    if (client != null) {
      span = client.startSpan("PromptTemplate", "prompt", SpanValues.V_SCENE_PROMPT_TEMPLATE);
    }

    try {
      List<Message> result = formatter.format(prompt, variables);

      if (span != null) {
        span.setTag("prompt_key", prompt.getPromptKey());
        span.setTag("prompt_version", prompt.getVersion());

        Map<String, Object> input = new HashMap<>();
        if (prompt.getPromptTemplate() != null) {
          input.put("messages", prompt.getPromptTemplate().getMessages());
        }
        input.put("variables", variables);
        span.setInput(JsonUtils.toJson(input));

        span.setOutput(JsonUtils.toJson(result));
      }

      return result;
    } catch (Exception e) {
      if (span != null) {
        span.setError(e);
      }
      throw e;
    } finally {
      if (span != null) {
        span.finish();
      }
    }
  }

  /**
   * Get and format prompt in one call.
   *
   * @param param the parameters for getting prompt
   * @param variables the variables to substitute
   * @return formatted messages
   */
  public List<Message> getAndFormat(GetPromptParam param, Map<String, Object> variables) {
    Prompt prompt = getPrompt(param);
    return formatPrompt(prompt, variables);
  }

  /**
   * Invalidate cache for a prompt.
   *
   * @param param the parameters identifying the prompt
   */
  public void invalidateCache(GetPromptParam param) {
    String cacheKey = buildCacheKey(param);
    cache.invalidate(cacheKey);
  }

  /** Invalidate all cached prompts. */
  public void invalidateAllCache() {
    cache.invalidateAll();
  }

  /**
   * Fetch prompt from server (called by cache loader). This method is called by the cache when a
   * prompt is not found in cache. Uses mget API with singleflight pattern to prevent duplicate
   * requests.
   *
   * @param cacheKey the cache key
   * @return prompt fetched from server
   */
  private Prompt fetchPromptFromServer(String cacheKey) {
    //        logger.info("Fetching prompt from server for cache key: {}", cacheKey);

    // Get parameters from map
    GetPromptParam param = paramMap.get(cacheKey);
    if (param == null) {
      // Fallback: try to parse from cache key if param not found
      logger.warn(
          "Param not found in map for cache key: {}, attempting to parse from key", cacheKey);
      param = parseParamFromCacheKey(cacheKey);
    }

    // Make param effectively final for lambda
    final GetPromptParam finalParam = param;

    // Build request for single query
    List<Map<String, Object>> queries = new ArrayList<>();
    Map<String, Object> query = new HashMap<>();
    query.put("prompt_key", finalParam.getPromptKey());
    if (finalParam.getVersion() != null && !finalParam.getVersion().isEmpty()) {
      query.put("version", finalParam.getVersion());
    }
    if (finalParam.getLabel() != null && !finalParam.getLabel().isEmpty()) {
      query.put("label", finalParam.getLabel());
    }
    queries.add(query);

    // Build request body for mget API
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("workspace_id", workspaceId);
    requestBody.put("queries", queries);

    // Generate singleflight key (sorted JSON of request)
    String singleflightKey = JsonUtils.toJson(requestBody);

    // Use singleflight pattern
    CompletableFuture<Prompt> future =
        singleflightMap.computeIfAbsent(
            singleflightKey,
            key -> {
              return CompletableFuture.supplyAsync(
                  () -> {
                    try {
                      return doMPullPrompt(requestBody, finalParam);
                    } finally {
                      // Remove from singleflight map after completion
                      singleflightMap.remove(singleflightKey);
                    }
                  });
            });

    try {
      return future.get();
    } catch (Exception e) {
      if (e.getCause() instanceof PromptException) {
        throw (PromptException) e.getCause();
      }
      throw new PromptException(
          ErrorCode.INTERNAL_ERROR,
          "Failed to fetch prompt from server: " + param.getPromptKey(),
          e);
    }
  }

  /**
   * Execute mget API request to fetch prompts.
   *
   * @param requestBody the request body
   * @param param the original parameter (for logging)
   * @return the fetched prompt
   */
  private Prompt doMPullPrompt(Map<String, Object> requestBody, GetPromptParam param) {
    try {
      logger.debug(
          "Requesting prompt from server: endpoint={}, body={}", promptEndpoint, requestBody);

      // Log request information including headers that will be sent
      //            logger.info("=== Request Prompt Details ===");
      //            logger.info("URL: {}", promptEndpoint);
      //            logger.info("Request Body: {}", JsonUtils.toJson(requestBody));
      //            logger.info("Prompt Key: {}", param.getPromptKey());
      //            if (param.getVersion() != null && !param.getVersion().isEmpty()) {
      //                logger.info("Version: {}", param.getVersion());
      //            }
      //            if (param.getLabel() != null && !param.getLabel().isEmpty()) {
      //                logger.info("Label: {}", param.getLabel());
      //            }

      // Make HTTP request to fetch prompt using mget API
      String response = httpClient.post(promptEndpoint, requestBody);

      if (response == null || response.isEmpty()) {
        throw new PromptException(
            ErrorCode.PROMPT_NOT_FOUND,
            "Empty response from server for prompt: " + param.getPromptKey());
      }

      //            logger.info("=== Response Received ===");
      //            logger.debug("Response body: {}", response);

      // Check if response is HTML (likely an error page or redirect)
      String trimmedResponse = response.trim();
      if (trimmedResponse.startsWith("<!")
          || trimmedResponse.startsWith("<html")
          || trimmedResponse.startsWith("<!doctype")) {
        logger.error("Received HTML response instead of JSON. This usually indicates:");
        logger.error("1. Authentication failure (check your credentials)");
        logger.error("2. Incorrect endpoint URL: {}", promptEndpoint);
        logger.error("3. Server redirecting to login/error page");
        logger.error(
            "Response preview (first 500 chars): {}",
            response.length() > 500 ? response.substring(0, 500) : response);
        throw new PromptException(
            ErrorCode.INTERNAL_ERROR,
            String.format(
                "Server returned HTML instead of JSON. "
                    + "This usually indicates authentication failure or incorrect endpoint. "
                    + "Endpoint: %s, Prompt Key: %s",
                promptEndpoint, param.getPromptKey()));
      }

      // Parse response
      Map<String, Object> responseMap;
      try {
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = (Map<String, Object>) JsonUtils.fromJson(response, Map.class);
        responseMap = parsed;
      } catch (Exception e) {
        logger.error(
            "Failed to parse response as JSON. Response preview (first 500 chars): {}",
            response.length() > 500 ? response.substring(0, 500) : response);
        throw new PromptException(
            ErrorCode.INTERNAL_ERROR,
            String.format(
                "Failed to parse response as JSON. "
                    + "This may indicate the server returned an error page. "
                    + "Endpoint: %s, Prompt Key: %s",
                promptEndpoint, param.getPromptKey()),
            e);
      }

      if (responseMap == null) {
        throw new PromptException(ErrorCode.INTERNAL_ERROR, "Failed to parse response from server");
      }

      // Parse mget response format: {data: {items: [{query: {...}, prompt: {...}}]}}
      if (responseMap.containsKey("data")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

        if (dataMap != null && dataMap.containsKey("items")) {
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> items = (List<Map<String, Object>>) dataMap.get("items");

          if (items != null && !items.isEmpty()) {
            // Get first item (should only be one for single query)
            Map<String, Object> item = items.get(0);
            if (item.containsKey("prompt")) {
              Object promptObj = item.get("prompt");
              String promptJson = JsonUtils.toJson(promptObj);
              Prompt prompt = JsonUtils.fromJson(promptJson, Prompt.class);

              if (prompt == null) {
                throw new PromptException(
                    ErrorCode.PROMPT_NOT_FOUND, "Failed to deserialize prompt from response");
              }

              //                            logger.info("Successfully fetched prompt from server:
              // {}", param.getPromptKey());
              return prompt;
            }
          }
        }
      }

      throw new PromptException(
          ErrorCode.PROMPT_NOT_FOUND, "Prompt not found in response. Response: " + response);
    } catch (PromptException e) {
      logger.error("Error fetching prompt from server for key: {}", param.getPromptKey(), e);
      throw e;
    } catch (Exception e) {
      logger.error(
          "Unexpected error fetching prompt from server for key: {}", param.getPromptKey(), e);
      throw new PromptException(
          ErrorCode.INTERNAL_ERROR,
          "Failed to fetch prompt from server: " + param.getPromptKey(),
          e);
    }
  }

  /**
   * Parse GetPromptParam from cache key (fallback method).
   *
   * @param cacheKey the cache key
   * @return GetPromptParam parsed from cache key
   */
  private GetPromptParam parseParamFromCacheKey(String cacheKey) {
    GetPromptParam param = new GetPromptParam();
    String[] parts = cacheKey.split(":", 3);
    if (parts.length > 0 && !parts[0].isEmpty()) {
      param.setPromptKey(parts[0]);
    }
    if (parts.length > 1 && !parts[1].isEmpty()) {
      param.setVersion(parts[1]);
    }
    if (parts.length > 2 && !parts[2].isEmpty()) {
      param.setLabel(parts[2]);
    }
    return param;
  }

  /** Build cache key from parameters. */
  private String buildCacheKey(GetPromptParam param) {
    StringBuilder key = new StringBuilder(param.getPromptKey());
    key.append(":");
    key.append(param.getVersion() != null ? param.getVersion() : "");
    key.append(":");
    key.append(param.getLabel() != null ? param.getLabel() : "");
    return key.toString();
  }

  /**
   * Execute a prompt.
   *
   * @param param the execution parameters
   * @return the execution result
   */
  public ExecuteResult execute(ExecuteParam param) {
    ValidationUtils.requireNonNull(param, "param");
    ValidationUtils.requireNonEmpty(param.getPromptKey(), "promptKey");

    if (executeEndpoint == null) {
      throw new PromptException(ErrorCode.INTERNAL_ERROR, "Execute endpoint is not configured");
    }

    try {
      // Build execute request
      Map<String, Object> requestBody = buildExecuteRequest(param);

      //            logger.info("=== Execute Prompt Request ===");
      //            logger.info("URL: {}", executeEndpoint);
      //            logger.info("Method: POST");
      //            logger.info("Request Body: {}", JsonUtils.toJson(requestBody));

      // Make HTTP request
      String response = httpClient.post(executeEndpoint, requestBody);

      if (response == null || response.isEmpty()) {
        throw new PromptException(
            ErrorCode.INTERNAL_ERROR, "Empty response from server for execute request");
      }

      //            logger.info("=== Execute Prompt Response ===");
      //            logger.debug("Response body: {}", response);

      // Parse response
      @SuppressWarnings("unchecked")
      Map<String, Object> responseMap = JsonUtils.fromJson(response, Map.class);

      if (responseMap == null) {
        throw new PromptException(ErrorCode.INTERNAL_ERROR, "Failed to parse response from server");
      }

      // Parse response format: {data: {message: {...}, finish_reason: "...", usage: {...}}}
      if (responseMap.containsKey("data")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

        ExecuteResult result = new ExecuteResult();

        if (dataMap != null) {
          if (dataMap.containsKey("message")) {
            Object messageObj = dataMap.get("message");
            String messageJson = JsonUtils.toJson(messageObj);
            Message message = JsonUtils.fromJson(messageJson, Message.class);
            result.setMessage(message);
          }

          if (dataMap.containsKey("finish_reason")) {
            Object finishReasonObj = dataMap.get("finish_reason");
            if (finishReasonObj != null) {
              result.setFinishReason(finishReasonObj.toString());
            }
          }

          if (dataMap.containsKey("usage")) {
            Object usageObj = dataMap.get("usage");
            String usageJson = JsonUtils.toJson(usageObj);
            TokenUsage usage = JsonUtils.fromJson(usageJson, TokenUsage.class);
            result.setUsage(usage);
          }
        }

        return result;
      } else {
        throw new PromptException(
            ErrorCode.INTERNAL_ERROR, "Invalid response format. Response: " + response);
      }
    } catch (PromptException e) {
      throw e;
    } catch (Exception e) {
      throw new PromptException(
          ErrorCode.INTERNAL_ERROR, "Failed to execute prompt: " + param.getPromptKey(), e);
    }
  }

  /**
   * Execute a prompt with streaming response.
   *
   * @param param the execution parameters
   * @return stream reader for ExecuteResult
   */
  public StreamReader<ExecuteResult> executeStreaming(ExecuteParam param) {
    ValidationUtils.requireNonNull(param, "param");
    ValidationUtils.requireNonEmpty(param.getPromptKey(), "promptKey");

    if (executeStreamingEndpoint == null) {
      throw new PromptException(
          ErrorCode.INTERNAL_ERROR, "Execute streaming endpoint is not configured");
    }

    try {
      // Build execute request
      Map<String, Object> requestBody = buildExecuteRequest(param);

      //            logger.info("=== Execute Streaming Prompt Request ===");
      //            logger.info("URL: {}", executeStreamingEndpoint);
      //            logger.info("Method: POST");
      //            logger.info("Request Body: {}", JsonUtils.toJson(requestBody));

      // Make streaming HTTP request
      Response response = httpClient.postStream(executeStreamingEndpoint, requestBody);

      // Get response body stream
      ResponseBody body = response.body();
      if (body == null) {
        response.close();
        throw new PromptException(ErrorCode.INTERNAL_ERROR, "Empty response body from server");
      }

      InputStream inputStream = body.byteStream();

      // Create SSE decoder and parser
      SSEDecoder decoder = new SSEDecoder(inputStream);
      SSEParser<ExecuteResult> parser = new ExecuteSSEParser();

      // Create stream reader
      return new StreamReader<ExecuteResult>(decoder, parser) {
        @Override
        public void close() throws IOException {
          super.close();
          response.close();
        }
      };
    } catch (IOException e) {
      throw new PromptException(
          ErrorCode.INTERNAL_ERROR,
          "Failed to execute streaming prompt: " + param.getPromptKey(),
          e);
    }
  }

  /**
   * Build execute request body from ExecuteParam.
   *
   * @param param the execution parameters
   * @return the request body map
   */
  private Map<String, Object> buildExecuteRequest(ExecuteParam param) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("workspace_id", workspaceId);

    // Build prompt_identifier
    Map<String, Object> promptIdentifier = new HashMap<>();
    promptIdentifier.put("prompt_key", param.getPromptKey());
    if (param.getVersion() != null && !param.getVersion().isEmpty()) {
      promptIdentifier.put("version", param.getVersion());
    }
    if (param.getLabel() != null && !param.getLabel().isEmpty()) {
      promptIdentifier.put("label", param.getLabel());
    }
    requestBody.put("prompt_identifier", promptIdentifier);

    // Build variable_vals
    if (param.getVariableVals() != null && !param.getVariableVals().isEmpty()) {
      List<Map<String, Object>> variableVals = new ArrayList<>();
      for (Map.Entry<String, Object> entry : param.getVariableVals().entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();

        if (value == null) {
          throw new PromptException(
              ErrorCode.INVALID_PARAM, "Variable value for key '" + key + "' is null");
        }

        Map<String, Object> variableVal = new HashMap<>();
        variableVal.put("key", key);

        // Handle different value types
        if (value instanceof String) {
          variableVal.put("value", value);
        } else if (value instanceof Message) {
          List<Message> messages = new ArrayList<>();
          messages.add((Message) value);
          variableVal.put("placeholder_messages", messages);
        } else if (value instanceof List) {
          List<?> list = (List<?>) value;
          if (!list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Message) {
              @SuppressWarnings("unchecked")
              List<Message> messages = (List<Message>) value;
              variableVal.put("placeholder_messages", messages);
            } else if (first instanceof ContentPart) {
              @SuppressWarnings("unchecked")
              List<ContentPart> parts = (List<ContentPart>) value;
              variableVal.put("multi_part_values", parts);
            } else {
              // Other types: serialize to JSON string
              String jsonValue = JsonUtils.toJson(value);
              variableVal.put("value", jsonValue);
            }
          } else {
            // Empty list: serialize to JSON string
            String jsonValue = JsonUtils.toJson(value);
            variableVal.put("value", jsonValue);
          }
        } else if (value instanceof ContentPart) {
          List<ContentPart> parts = new ArrayList<>();
          parts.add((ContentPart) value);
          variableVal.put("multi_part_values", parts);
        } else {
          // Other types: serialize to JSON string
          String jsonValue = JsonUtils.toJson(value);
          variableVal.put("value", jsonValue);
        }

        variableVals.add(variableVal);
      }
      requestBody.put("variable_vals", variableVals);
    }

    // Add messages if provided
    if (param.getMessages() != null && !param.getMessages().isEmpty()) {
      requestBody.put("messages", param.getMessages());
    }

    return requestBody;
  }

  /** SSE Parser for ExecuteResult. */
  private static class ExecuteSSEParser implements SSEParser<ExecuteResult> {
    @Override
    public ExecuteResult parse(ServerSentEvent sse) throws Exception {
      if (sse == null || !sse.hasData()) {
        return null;
      }

      // Parse streaming response
      @SuppressWarnings("unchecked")
      Map<String, Object> dataMap = JsonUtils.fromJson(sse.getData(), Map.class);

      if (dataMap == null) {
        return null;
      }

      ExecuteResult result = new ExecuteResult();

      if (dataMap.containsKey("message")) {
        Object messageObj = dataMap.get("message");
        String messageJson = JsonUtils.toJson(messageObj);
        Message message = JsonUtils.fromJson(messageJson, Message.class);
        result.setMessage(message);
      }

      if (dataMap.containsKey("finish_reason")) {
        Object finishReasonObj = dataMap.get("finish_reason");
        if (finishReasonObj != null) {
          result.setFinishReason(finishReasonObj.toString());
        }
      }

      if (dataMap.containsKey("usage")) {
        Object usageObj = dataMap.get("usage");
        String usageJson = JsonUtils.toJson(usageObj);
        TokenUsage usage = JsonUtils.fromJson(usageJson, TokenUsage.class);
        result.setUsage(usage);
      }

      return result;
    }

    @Override
    public Exception handleError(ServerSentEvent sse) {
      if (sse == null || sse.getEvent() == null) {
        return null;
      }

      // Check if event field contains "error" (case-insensitive)
      String event = sse.getEvent().toLowerCase();
      if (event.contains("error")) {
        String errorMsg = sse.getData();
        if (errorMsg == null || errorMsg.isEmpty()) {
          errorMsg = "Error event received without data";
        }
        return new PromptException(ErrorCode.INTERNAL_ERROR, errorMsg);
      }

      return null;
    }
  }
}
