package com.coze.loop.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coze.loop.internal.Constants;
import com.coze.loop.internal.JsonUtils;
import com.coze.loop.spec.tracespec.Runtime;
import com.coze.loop.spec.tracespec.SpanKeys;
import com.coze.loop.spec.tracespec.SpanValues;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;

/**
 * Wrapper for OpenTelemetry Span that provides CozeLoop-specific methods.
 *
 * <p>This class wraps OpenTelemetry's {@link Span} and provides:
 *
 * <ul>
 *   <li>CozeLoop-specific convenience methods (setInput, setOutput, setModel, etc.)
 *   <li>Automatic scope management via try-with-resources
 *   <li>Access to underlying OpenTelemetry Span for advanced usage
 * </ul>
 *
 * <p>The span is automatically made current in the OpenTelemetry context when created, allowing
 * child spans to automatically inherit the parent context.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
 *     span.setInput("input data");
 *     span.setOutput("output data");
 *     span.addEvent("important-event");
 * }
 * }</pre>
 *
 * <p>For advanced OpenTelemetry features, you can access the underlying Span:
 *
 * <pre>{@code
 * CozeLoopSpan cozeSpan = client.startSpan("operation", "custom");
 * Span otelSpan = cozeSpan.getSpan();
 * // Use OpenTelemetry APIs directly
 * }</pre>
 */
public class CozeLoopSpan implements AutoCloseable {
  private final Span span;
  private final Scope scope;
  private final ContextPropagators propagators;
  private final String scene;
  private final List<Scope> extraScopes = new ArrayList<>();
  private Context context;
  private Runtime runtime;

  /**
   * Create a new CozeLoopSpan wrapper.
   *
   * <p>The span is automatically made current in the OpenTelemetry context, which enables automatic
   * context propagation to child spans.
   *
   * @param span the underlying OpenTelemetry Span
   * @param scope the scope that makes this span current in the context
   * @param propagators the context propagators for header injection
   * @param context the context this span was created in
   * @param scene the scene identifier for this span
   */
  public CozeLoopSpan(
      Span span, Scope scope, ContextPropagators propagators, Context context, String scene) {
    this.span = span;
    this.scope = scope;
    this.propagators = propagators;
    this.context = context;
    this.scene = (scene == null || scene.isEmpty()) ? SpanValues.V_SCENE_CUSTOM : scene;
  }

  /**
   * Get the trace ID for this span.
   *
   * @return the trace ID
   */
  public String getTraceID() {
    return span.getSpanContext().getTraceId();
  }

  /**
   * Get the span ID for this span.
   *
   * @return the span ID
   */
  public String getSpanID() {
    return span.getSpanContext().getSpanId();
  }

  /**
   * Get all baggage items.
   *
   * @return map of baggage keys and values
   */
  public Map<String, String> getBaggage() {
    Map<String, String> result = new HashMap<>();
    Baggage.fromContext(context).forEach((key, value) -> result.put(key, value.getValue()));
    return result;
  }

  /**
   * Set the input for this span. You can find recommended specification in
   * https://github.com/coze-dev/cozeloop-java/tree/main/cozeloop-core/src/main/java/com/coze/loop/spec/tracespec.
   * Or you can use any struct you like.
   *
   * @param input the input object
   * @return this span
   */
  public CozeLoopSpan setInput(Object input) {
    if (input != null) {
      String inputStr = input instanceof String ? (String) input : JsonUtils.toJson(input);
      span.setAttribute(AttributeKey.stringKey(SpanKeys.COZELOOP_INPUT), inputStr);
    }
    return this;
  }

  /**
   * Set the output for this span. You can find recommended specification in
   * https://github.com/coze-dev/cozeloop-java/tree/main/cozeloop-core/src/main/java/com/coze/loop/spec/tracespec.
   * Or you can use any struct you like.
   *
   * @param output the output object
   * @return this span
   */
  public CozeLoopSpan setOutput(Object output) {
    if (output != null) {
      String outputStr = output instanceof String ? (String) output : JsonUtils.toJson(output);
      span.setAttribute(AttributeKey.stringKey(SpanKeys.COZELOOP_OUTPUT), outputStr);
    }
    return this;
  }

  /**
   * Set the error for this span.
   *
   * @param error the error/exception
   * @return this span
   */
  public CozeLoopSpan setError(Throwable error) {
    if (error != null) {
      span.setAttribute(SpanKeys.COZELOOP_ERROR, error.getMessage());
      span.recordException(error);
    }
    return this;
  }

  /**
   * Set status code (0=OK, other=ERROR).
   *
   * @param statusCode the status code
   * @return this span
   */
  public CozeLoopSpan setStatusCode(int statusCode) {
    span.setAttribute(SpanKeys.COZELOOP_STATUS_CODE, statusCode);
    return this;
  }

  /**
   * Set user_id for this span.
   *
   * @param userId the user ID
   * @return this span
   */
  public CozeLoopSpan setUserID(String userId) {
    if (userId != null) {
      span.setAttribute(SpanKeys.USER_ID, userId);
    }
    return this;
  }

  /**
   * Set user_id baggage for this span and future child spans.
   *
   * @param userId the user ID
   * @return this span
   */
  public CozeLoopSpan setUserIDBaggage(String userId) {
    return setBaggage(SpanKeys.USER_ID, userId);
  }

  /**
   * Set message_id for this span.
   *
   * @param messageId the message ID
   * @return this span
   */
  public CozeLoopSpan setMessageID(String messageId) {
    if (messageId != null) {
      span.setAttribute(SpanKeys.MESSAGING_MESSAGE_ID, messageId);
    }
    return this;
  }

  /**
   * Set message_id baggage for this span and future child spans.
   *
   * @param messageId the message ID
   * @return this span
   */
  public CozeLoopSpan setMessageIDBaggage(String messageId) {
    return setBaggage(SpanKeys.MESSAGING_MESSAGE_ID, messageId);
  }

  /**
   * Set thread_id for this span.
   *
   * @param threadId the thread ID
   * @return this span
   */
  public CozeLoopSpan setThreadID(String threadId) {
    if (threadId != null) {
      span.setAttribute(SpanKeys.SESSION_ID, threadId);
    }
    return this;
  }

  /**
   * Set thread_id baggage for this span and future child spans.
   *
   * @param threadId the thread ID
   * @return this span
   */
  public CozeLoopSpan setThreadIDBaggage(String threadId) {
    return setBaggage(SpanKeys.SESSION_ID, threadId);
  }

  /**
   * Set prompt ID or name.
   *
   * @param prompt the prompt identifier
   * @return this span
   */
  // todo: implement after trace spec is defined
  //    public CozeLoopSpan setPrompt(String prompt) {
  //        if (prompt != null) {
  //            span.setAttribute("cozeloop.prompt", prompt);
  //        }
  //        return this;
  //    }

  /**
   * Set model provider (e.g., "openai", "anthropic").
   *
   * @param provider the model provider
   * @return this span
   */
  public CozeLoopSpan setModelProvider(String provider) {
    if (provider != null) {
      span.setAttribute(AttributeKey.stringKey(SpanKeys.GEN_AI_PROVIDER_NAME), provider);
    }
    return this;
  }

  /**
   * Set model name.
   *
   * @param model the model name
   * @return this span
   */
  public CozeLoopSpan setModelName(String model) {
    if (model != null) {
      span.setAttribute(AttributeKey.stringKey(SpanKeys.LLM_MODEL_NAME), model);
    }
    return this;
  }

  /**
   * Set model temperature.
   *
   * @param temperature the temperature
   * @return this span
   */
  public CozeLoopSpan setModelTemperature(Double temperature) {
    if (temperature != null) {
      span.setAttribute(AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_TEMPERATURE), temperature);
    }
    return this;
  }

  /**
   * Set model top_p.
   *
   * @param topP the top_p
   * @return this span
   */
  public CozeLoopSpan setModelTopP(Double topP) {
    if (topP != null) {
      span.setAttribute(AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_TOP_P), topP);
    }
    return this;
  }

  /**
   * Set model top_k.
   *
   * @param topK the top_k
   * @return this span
   */
  public CozeLoopSpan setModelTopK(Integer topK) {
    if (topK != null) {
      span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_REQUEST_TOP_K), topK);
    }
    return this;
  }

  /**
   * Set model max_tokens.
   *
   * @param maxTokens the max_tokens
   * @return this span
   */
  public CozeLoopSpan setModelMaxTokens(Integer maxTokens) {
    if (maxTokens != null) {
      span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_REQUEST_MAX_TOKENS), maxTokens);
    }
    return this;
  }

  /**
   * Set model frequency_penalty.
   *
   * @param frequencyPenalty the frequency_penalty
   * @return this span
   */
  public CozeLoopSpan setModelFrequencyPenalty(Double frequencyPenalty) {
    if (frequencyPenalty != null) {
      span.setAttribute(
          AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_FREQUENCY_PENALTY), frequencyPenalty);
    }
    return this;
  }

  /**
   * Set model presence_penalty.
   *
   * @param presencePenalty the presence_penalty
   * @return this span
   */
  public CozeLoopSpan setModelPresencePenalty(Double presencePenalty) {
    if (presencePenalty != null) {
      span.setAttribute(
          AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_PRESENCE_PENALTY), presencePenalty);
    }
    return this;
  }

  /**
   * Set model stop_sequences.
   *
   * @param stopSequences the stop_sequences
   * @return this span
   */
  public CozeLoopSpan setModelStopSequences(List<String> stopSequences) {
    if (stopSequences != null && !stopSequences.isEmpty()) {
      span.setAttribute(
          AttributeKey.stringArrayKey(SpanKeys.GEN_AI_REQUEST_STOP_SEQUENCES), stopSequences);
    }
    return this;
  }

  /**
   * Set input tokens.
   *
   * @param tokens the number of input tokens
   * @return this span
   */
  public CozeLoopSpan setInputTokens(long tokens) {
    span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_USAGE_INPUT_TOKENS), tokens);
    return this;
  }

  /**
   * Set output tokens.
   *
   * @param tokens the number of output tokens
   * @return this span
   */
  public CozeLoopSpan setOutputTokens(long tokens) {
    span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_USAGE_OUTPUT_TOKENS), tokens);
    return this;
  }

  /**
   * Set start time (Not duration!) of first response for streaming calls.
   *
   * @param timestampUs the timestamp in microseconds
   * @return this span
   */
  public CozeLoopSpan setStartTimeFirstResp(long timestampUs) {
    span.setAttribute(SpanKeys.COZELOOP_TIME_TO_FIRST_TOKEN, timestampUs);
    return this;
  }

  /**
   * Set runtime data. Do not set unless you know what you are doing.
   *
   * @param runt Runtime
   * @return this span
   */
  public CozeLoopSpan setRuntime(Runtime runt) {
    runtime = runt;
    return this;
  }

  /**
   * Set service name for this span.
   *
   * @param serviceName the service name
   * @return this span
   */
  public CozeLoopSpan setServiceName(String serviceName) {
    if (serviceName != null) {
      span.setAttribute(SpanKeys.SERVICE_NAME, serviceName);
    }
    return this;
  }

  /**
   * Set log ID for this span.
   *
   * @param logID the log ID
   * @return this span
   */
  public CozeLoopSpan setLogID(String logID) {
    if (logID != null) {
      span.setAttribute(SpanKeys.COZELOOP_LOGID, logID);
    }
    return this;
  }

  // todo：not implement set finish time
  // public CozeLoopSpan SetFinishTime(Date finishTime) {
  //  span.setAttribute("cozeloop.finish_time", finishTime);
  //  return this;
  // }

  // todo：not implement set system tags
  // public CozeLoopSpan SetFinishTime(Date finishTime) {
  //  span.setAttribute("cozeloop.finish_time", finishTime);
  //  return this;
  // }

  /**
   * Set deployment environment (e.g., "prod", "staging").
   *
   * @param env the environment
   * @return this span
   */
  public CozeLoopSpan setDeploymentEnv(String env) {
    if (env != null) {
      span.setAttribute(SpanKeys.DEPLOYMENT_ENV, env);
    }
    return this;
  }

  /**
   * Set multiple tags/attributes at once.
   *
   * @param tags map of tags
   * @return this span
   */
  public CozeLoopSpan setTags(Map<String, String> tags) {
    if (tags != null) {
      tags.forEach(
          (k, v) -> {
            if (k != null && v != null) {
              span.setAttribute(k, v);
            }
          });
    }
    return this;
  }

  /**
   * Set baggage for this span and future child spans. Baggage is automatically propagated across
   * service boundaries.
   *
   * @param key the baggage key
   * @param value the baggage value
   * @return this span
   */
  public CozeLoopSpan setBaggage(String key, String value) {
    if (key != null && value != null) {
      // Also set as span attribute for visibility in the trace
      span.setAttribute(key, value);

      Baggage baggage = Baggage.fromContext(context).toBuilder().put(key, value).build();
      this.context = context.with(baggage);
      extraScopes.add(this.context.makeCurrent());
    }
    return this;
  }

  /**
   * Set multiple baggage items.
   *
   * @param values map of baggage keys and values
   * @return this span
   */
  public CozeLoopSpan setBaggage(Map<String, String> values) {
    if (values != null && !values.isEmpty()) {
      io.opentelemetry.api.baggage.BaggageBuilder builder =
          Baggage.fromContext(context).toBuilder();
      for (Map.Entry<String, String> entry : values.entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null) {
          span.setAttribute(entry.getKey(), entry.getValue());
          builder.put(entry.getKey(), entry.getValue());
        }
      }
      this.context = context.with(builder.build());
      extraScopes.add(this.context.makeCurrent());
    }
    return this;
  }

  /**
   * Get headers for cross-service propagation.
   *
   * @return headers containing trace context
   */
  public Map<String, String> toHeader() {
    Map<String, String> headers = new HashMap<>();
    injectToHeaders(headers);
    return headers;
  }

  /**
   * Inject current context (Trace ID, Span ID, Baggage) into headers for cross-service propagation.
   *
   * @param carrier the map to inject headers into (e.g. HTTP request headers)
   */
  public void injectToHeaders(Map<String, String> carrier) {
    if (propagators != null) {
      propagators.getTextMapPropagator().inject(this.context, carrier, Map::put);
    }
  }

  /**
   * Set custom attribute (string).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @return this span
   */
  public CozeLoopSpan setTag(String key, String value) {
    if (key != null && value != null) {
      span.setAttribute(AttributeKey.stringKey(key), value);
    }
    return this;
  }

  /**
   * Set custom attribute (long).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @return this span
   */
  public CozeLoopSpan setTag(String key, long value) {
    if (key != null) {
      span.setAttribute(AttributeKey.longKey(key), value);
    }
    return this;
  }

  /**
   * Set custom attribute (double).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @return this span
   */
  public CozeLoopSpan setTag(String key, double value) {
    if (key != null) {
      span.setAttribute(AttributeKey.doubleKey(key), value);
    }
    return this;
  }

  /**
   * Set custom attribute (boolean).
   *
   * @param key the attribute key
   * @param value the attribute value
   * @return this span
   */
  public CozeLoopSpan setTag(String key, boolean value) {
    if (key != null) {
      span.setAttribute(AttributeKey.booleanKey(key), value);
    }
    return this;
  }

  /**
   * Add an event to this span.
   *
   * <p>Events are timestamped annotations on a span that represent something that happened during
   * the span's lifetime. They are useful for marking important milestones or state changes.
   *
   * <p>Example:
   *
   * <pre>{@code
   * span.addEvent("operation-started");
   * span.addEvent("data-processed");
   * span.addEvent("operation-completed");
   * }</pre>
   *
   * @param eventName the name of the event
   * @return this span
   */
  public CozeLoopSpan addEvent(String eventName) {
    if (eventName != null) {
      span.addEvent(eventName);
    }
    return this;
  }

  /**
   * Add an event with attributes to this span.
   *
   * <p>Events are timestamped annotations on a span. This method allows you to add an event with
   * associated attributes. The attributes are set on the span with an "event." prefix to
   * distinguish them from regular span attributes.
   *
   * <p>Example:
   *
   * <pre>{@code
   * Map<String, Object> eventAttrs = new HashMap<>();
   * eventAttrs.put("response_length", 150L);
   * eventAttrs.put("model", "gpt-4");
   * span.addEvent("llm-response-received", eventAttrs);
   * }</pre>
   *
   * <p>For more advanced event attributes, you can use the underlying OpenTelemetry Span:
   *
   * <pre>{@code
   * import io.opentelemetry.api.common.Attributes;
   * span.getSpan().addEvent("event-name",
   *     Attributes.of(AttributeKey.stringKey("key"), "value"));
   * }</pre>
   *
   * @param eventName the name of the event
   * @param attributes the attributes to attach to the event (will be prefixed with "event.")
   * @return this span
   */
  public CozeLoopSpan addEvent(String eventName, Map<String, Object> attributes) {
    if (eventName != null && attributes != null && !attributes.isEmpty()) {
      // Set attributes on the span with "event." prefix to associate them with the event
      for (Map.Entry<String, Object> entry : attributes.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (key == null || value == null) {
          continue;
        }

        // Set attributes with "event." prefix to associate with the event
        String eventAttrKey = "event." + key;
        if (value instanceof String) {
          span.setAttribute(AttributeKey.stringKey(eventAttrKey), (String) value);
        } else if (value instanceof Long) {
          span.setAttribute(AttributeKey.longKey(eventAttrKey), (Long) value);
        } else if (value instanceof Integer) {
          span.setAttribute(AttributeKey.longKey(eventAttrKey), ((Integer) value).longValue());
        } else if (value instanceof Double) {
          span.setAttribute(AttributeKey.doubleKey(eventAttrKey), (Double) value);
        } else if (value instanceof Float) {
          span.setAttribute(AttributeKey.doubleKey(eventAttrKey), ((Float) value).doubleValue());
        } else if (value instanceof Boolean) {
          span.setAttribute(AttributeKey.booleanKey(eventAttrKey), (Boolean) value);
        } else {
          // Convert other types to string
          span.setAttribute(AttributeKey.stringKey(eventAttrKey), String.valueOf(value));
        }
      }
      // Add the event
      span.addEvent(eventName);
    }
    return this;
  }

  /**
   * Record an exception on this span.
   *
   * <p>This is a convenience method that records an exception as an event with exception details.
   * It's equivalent to calling setError() but provides more detailed exception information.
   *
   * <p>Example:
   *
   * <pre>{@code
   * try {
   *     // operation
   * } catch (Exception e) {
   *     span.recordException(e);
   *     span.setStatusCode(1);
   *     throw e;
   * }
   * }</pre>
   *
   * @param exception the exception to record
   * @return this span
   */
  public CozeLoopSpan recordException(Throwable exception) {
    if (exception != null) {
      span.recordException(exception);
    }
    return this;
  }

  /**
   * Get the current OpenTelemetry Context.
   *
   * <p>This is useful for accessing the current context, which includes:
   *
   * <ul>
   *   <li>Current span
   *   <li>Baggage (key-value data propagated across services)
   *   <li>Other context data
   * </ul>
   *
   * <p>Example:
   *
   * <pre>{@code
   * Context currentContext = span.getCurrentContext();
   * // Use context for async operations or cross-service propagation
   * }</pre>
   *
   * @return the current OpenTelemetry context
   */
  public Context getCurrentContext() {
    return Context.current();
  }

  /**
   * Get the span context for this span.
   *
   * <p>The span context contains trace ID, span ID, trace flags, and trace state. It can be used to
   * create links to this span from other spans, or to propagate trace context across service
   * boundaries.
   *
   * <p>Example:
   *
   * <pre>{@code
   * SpanContext spanContext = span.getSpanContext();
   * // Pass spanContext to another service or span
   * }</pre>
   *
   * @return the span context
   */
  public io.opentelemetry.api.trace.SpanContext getSpanContext() {
    return span.getSpanContext();
  }

  /**
   * Get the underlying OpenTelemetry Span.
   *
   * <p>This method provides direct access to the underlying OpenTelemetry Span for advanced use
   * cases that require OpenTelemetry-specific APIs not exposed through CozeLoopSpan.
   *
   * <p>Example:
   *
   * <pre>{@code
   * Span otelSpan = span.getSpan();
   * // Use OpenTelemetry APIs directly
   * otelSpan.setAttribute(AttributeKey.stringKey("custom"), "value");
   * }</pre>
   *
   * @return the underlying OpenTelemetry span
   */
  public Span getSpan() {
    return span;
  }

  /**
   * Get the scope associated with this span.
   *
   * <p>The scope is what makes this span "current" in the OpenTelemetry context. When the scope is
   * closed, the span is no longer current. This is automatically handled by the try-with-resources
   * pattern, but can be accessed for advanced use cases.
   *
   * @return the scope
   */
  public Scope getScope() {
    return scope;
  }

  public void finish() {
    try {
      setSystemTag();
      span.end();
    } finally {
      for (int i = extraScopes.size() - 1; i >= 0; i--) {
        extraScopes.get(i).close();
      }
      scope.close();
    }
  }

  private void setSystemTag() {
    if (runtime == null) {
      runtime = new Runtime();
    }
    runtime.setLanguage("java");
    runtime.setLoopSDKVersion(Constants.Version);
    runtime.setScene(this.scene != null ? this.scene : "custom");

    span.setAttribute(SpanKeys.COZELOOP_SYSTEM_TAG_RUNTIME, JsonUtils.toJson(runtime));
  }

  /**
   * End the span and close the scope.
   *
   * <p>This method is automatically called when using try-with-resources. It ends the span (marking
   * it as finished) and closes the scope (removing it from the current context). The span will then
   * be processed by the BatchSpanProcessor and eventually exported to CozeLoop platform.
   */
  @Override
  public void close() {
    finish();
  }
}
