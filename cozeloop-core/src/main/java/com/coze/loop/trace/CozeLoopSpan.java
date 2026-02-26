package com.coze.loop.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.coze.loop.internal.Constants;
import com.coze.loop.internal.CozeLoopLogger;
import com.coze.loop.internal.JsonUtils;
import com.coze.loop.spec.tracespec.Runtime;
import com.coze.loop.spec.tracespec.SpanKeys;
import com.coze.loop.spec.tracespec.SpanValues;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
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
  private static final Logger logger = CozeLoopLogger.getLogger(CozeLoopSpan.class);

  private final Span span;
  private final Scope scope;
  private final ContextPropagators propagators;
  private final String scene;
  private final List<Scope> extraScopes = new ArrayList<>();
  private final FinishEventProcessor finishEventProcessor;
  private final long startTimeNanos;
  private CozeLoopContext context;
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
   * @param finishEventProcessor the processor to handle span finish events
   */
  public CozeLoopSpan(
      Span span,
      Scope scope,
      ContextPropagators propagators,
      CozeLoopContext context,
      String scene,
      FinishEventProcessor finishEventProcessor) {
    this.span = span;
    this.scope = scope;
    this.propagators = propagators;
    this.context = context;
    this.scene = (scene == null || scene.isEmpty()) ? SpanValues.V_SCENE_CUSTOM : scene;
    this.finishEventProcessor = finishEventProcessor;
    this.startTimeNanos = System.nanoTime();
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
   */
  public void setInput(Object input) {
    if (input != null) {
      String inputStr = input instanceof String ? (String) input : JsonUtils.toJson(input);
      span.setAttribute(AttributeKey.stringKey(SpanKeys.COZELOOP_INPUT), inputStr);
    }
  }

  /**
   * Set the output for this span. You can find recommended specification in
   * https://github.com/coze-dev/cozeloop-java/tree/main/cozeloop-core/src/main/java/com/coze/loop/spec/tracespec.
   * Or you can use any struct you like.
   *
   * @param output the output object
   */
  public void setOutput(Object output) {
    if (output != null) {
      String outputStr = output instanceof String ? (String) output : JsonUtils.toJson(output);
      span.setAttribute(AttributeKey.stringKey(SpanKeys.COZELOOP_OUTPUT), outputStr);
    }
  }

  /**
   * Set the error for this span.
   *
   * @param error the error/exception
   */
  public void setError(Throwable error) {
    if (error != null) {
      span.setAttribute(SpanKeys.COZELOOP_ERROR, error.getMessage());
      span.recordException(error);
    }
  }

  /**
   * Set status code (0=OK, other=ERROR).
   *
   * @param statusCode the status code
   */
  public void setStatusCode(int statusCode) {
    span.setAttribute(SpanKeys.COZELOOP_STATUS_CODE, statusCode);
  }

  /**
   * Set user_id for this span.
   *
   * @param userId the user ID
   */
  public void setUserID(String userId) {
    if (userId != null) {
      span.setAttribute(SpanKeys.USER_ID, userId);
    }
  }

  /**
   * Set user_id baggage for this span and future child spans.
   *
   * @param userId the user ID
   */
  public void setUserIDBaggage(String userId) {
    setBaggage(SpanKeys.USER_ID, userId);
  }

  /**
   * Set message_id for this span.
   *
   * @param messageId the message ID
   */
  public void setMessageID(String messageId) {
    if (messageId != null) {
      span.setAttribute(SpanKeys.MESSAGING_MESSAGE_ID, messageId);
    }
  }

  /**
   * Set message_id baggage for this span and future child spans.
   *
   * @param messageId the message ID
   */
  public void setMessageIDBaggage(String messageId) {
    setBaggage(SpanKeys.MESSAGING_MESSAGE_ID, messageId);
  }

  /**
   * Set thread_id for this span.
   *
   * @param threadId the thread ID
   */
  public void setThreadID(String threadId) {
    if (threadId != null) {
      span.setAttribute(SpanKeys.SESSION_ID, threadId);
    }
  }

  /**
   * Set thread_id baggage for this span and future child spans.
   *
   * @param threadId the thread ID
   */
  public void setThreadIDBaggage(String threadId) {
    setBaggage(SpanKeys.SESSION_ID, threadId);
  }

  // todo：not implement set system tags
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
   */
  public void setModelProvider(String provider) {
    if (provider != null) {
      span.setAttribute(AttributeKey.stringKey(SpanKeys.GEN_AI_PROVIDER_NAME), provider);
    }
  }

  /**
   * Set model name.
   *
   * @param model the model name
   */
  public void setModelName(String model) {
    if (model != null) {
      span.setAttribute(AttributeKey.stringKey(SpanKeys.LLM_MODEL_NAME), model);
    }
  }

  /**
   * Set model temperature.
   *
   * @param temperature the temperature
   */
  public void setModelTemperature(Double temperature) {
    if (temperature != null) {
      span.setAttribute(AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_TEMPERATURE), temperature);
    }
  }

  /**
   * Set model top_p.
   *
   * @param topP the top_p
   */
  public void setModelTopP(Double topP) {
    if (topP != null) {
      span.setAttribute(AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_TOP_P), topP);
    }
  }

  /**
   * Set model top_k.
   *
   * @param topK the top_k
   */
  public void setModelTopK(Integer topK) {
    if (topK != null) {
      span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_REQUEST_TOP_K), topK);
    }
  }

  /**
   * Set model max_tokens.
   *
   * @param maxTokens the max_tokens
   */
  public void setModelMaxTokens(Integer maxTokens) {
    if (maxTokens != null) {
      span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_REQUEST_MAX_TOKENS), maxTokens);
    }
  }

  /**
   * Set model frequency_penalty.
   *
   * @param frequencyPenalty the frequency_penalty
   */
  public void setModelFrequencyPenalty(Double frequencyPenalty) {
    if (frequencyPenalty != null) {
      span.setAttribute(
          AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_FREQUENCY_PENALTY), frequencyPenalty);
    }
  }

  /**
   * Set model presence_penalty.
   *
   * @param presencePenalty the presence_penalty
   */
  public void setModelPresencePenalty(Double presencePenalty) {
    if (presencePenalty != null) {
      span.setAttribute(
          AttributeKey.doubleKey(SpanKeys.GEN_AI_REQUEST_PRESENCE_PENALTY), presencePenalty);
    }
  }

  /**
   * Set model stop_sequences.
   *
   * @param stopSequences the stop_sequences
   */
  public void setModelStopSequences(List<String> stopSequences) {
    if (stopSequences != null && !stopSequences.isEmpty()) {
      span.setAttribute(
          AttributeKey.stringArrayKey(SpanKeys.GEN_AI_REQUEST_STOP_SEQUENCES), stopSequences);
    }
  }

  /**
   * Set input tokens.
   *
   * @param tokens the number of input tokens
   */
  public void setInputTokens(long tokens) {
    span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_USAGE_INPUT_TOKENS), tokens);
  }

  /**
   * Set output tokens.
   *
   * @param tokens the number of output tokens
   */
  public void setOutputTokens(long tokens) {
    span.setAttribute(AttributeKey.longKey(SpanKeys.GEN_AI_USAGE_OUTPUT_TOKENS), tokens);
  }

  /**
   * Set start time (Not duration!) of first response for streaming calls.
   *
   * @param timestampUs the timestamp in microseconds
   */
  public void setStartTimeFirstResp(long timestampUs) {
    span.setAttribute(SpanKeys.COZELOOP_TIME_TO_FIRST_TOKEN, timestampUs);
  }

  /**
   * Set runtime data. Do not set unless you know what you are doing.
   *
   * @param runt Runtime
   */
  public void setRuntime(Runtime runt) {
    runtime = runt;
  }

  /**
   * Set service name for this span.
   *
   * @param serviceName the service name
   */
  public void setServiceName(String serviceName) {
    if (serviceName != null) {
      span.setAttribute(SpanKeys.SERVICE_NAME, serviceName);
    }
  }

  /**
   * Set log ID for this span.
   *
   * @param logID the log ID
   */
  public void setLogID(String logID) {
    if (logID != null) {
      span.setAttribute(SpanKeys.COZELOOP_LOGID, logID);
    }
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
   */
  public void setDeploymentEnv(String env) {
    if (env != null) {
      span.setAttribute(SpanKeys.DEPLOYMENT_ENV, env);
    }
  }

  /**
   * Set multiple tags/attributes at once.
   *
   * @param tags map of tags
   */
  public void setTags(Map<String, String> tags) {
    if (tags != null) {
      tags.forEach(
          (k, v) -> {
            if (k != null && v != null) {
              span.setAttribute(k, v);
            }
          });
    }
  }

  /**
   * Set baggage for this span and future child spans. Baggage is automatically propagated across
   * service boundaries.
   *
   * @param key the baggage key
   * @param value the baggage value
   */
  public void setBaggage(String key, String value) {
    if (key != null && value != null) {
      // Also set as span attribute for visibility in the trace
      span.setAttribute(key, value);

      Baggage baggage = Baggage.fromContext(context).toBuilder().put(key, value).build();
      this.context = new CozeLoopContext(context.with(baggage));
      extraScopes.add(this.context.makeCurrent());
    }
  }

  /**
   * Set multiple baggage items.
   *
   * @param values map of baggage keys and values
   */
  public void setBaggage(Map<String, String> values) {
    if (values != null && !values.isEmpty()) {
      io.opentelemetry.api.baggage.BaggageBuilder builder =
          Baggage.fromContext(context).toBuilder();
      for (Map.Entry<String, String> entry : values.entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null) {
          span.setAttribute(entry.getKey(), entry.getValue());
          builder.put(entry.getKey(), entry.getValue());
        }
      }
      this.context = new CozeLoopContext(context.with(builder.build()));
      extraScopes.add(this.context.makeCurrent());
    }
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
  private void injectToHeaders(Map<String, String> carrier) {
    if (propagators != null) {
      propagators.getTextMapPropagator().inject(this.context, carrier, Map::put);
    }
  }

  /**
   * Set custom attribute (string).
   *
   * @param key the attribute key
   * @param value the attribute value
   */
  public void setTag(String key, String value) {
    if (key != null && value != null) {
      span.setAttribute(AttributeKey.stringKey(key), value);
    }
  }

  /**
   * Set custom attribute (long).
   *
   * @param key the attribute key
   * @param value the attribute value
   */
  public void setTag(String key, long value) {
    if (key != null) {
      span.setAttribute(AttributeKey.longKey(key), value);
    }
  }

  /**
   * Set custom attribute (double).
   *
   * @param key the attribute key
   * @param value the attribute value
   */
  public void setTag(String key, double value) {
    if (key != null) {
      span.setAttribute(AttributeKey.doubleKey(key), value);
    }
  }

  /**
   * Set custom attribute (boolean).
   *
   * @param key the attribute key
   * @param value the attribute value
   */
  public void setTag(String key, boolean value) {
    if (key != null) {
      span.setAttribute(AttributeKey.booleanKey(key), value);
    }
  }

  /**
   * Get the CozeLoop Context associated with this span.
   *
   * <p>This context includes the current span, baggage, and other propagation data. It can be used
   * to create child spans or for manual context propagation.
   *
   * @return the CozeLoop context
   */
  public CozeLoopContext getContext() {
    return context;
  }

  /**
   * End the span and close the scope.
   *
   * <p>This method is automatically called when using try-with-resources. It ends the span (marking
   * it as finished) and closes the scope (removing it from the current context). The span will then
   * be processed by the BatchSpanProcessor and eventually exported to CozeLoop platform.
   */
  public void finish() {
    try {
      setSystemTag();
      span.end();
      notifyFinishEvent();
    } finally {
      for (int i = extraScopes.size() - 1; i >= 0; i--) {
        extraScopes.get(i).close();
      }
      scope.close();
    }
  }

  private void notifyFinishEvent() {
    if (finishEventProcessor != null) {
      try {
        Span parentSpan = Span.fromContextOrNull(context);
        boolean isRootSpan =
            parentSpan == null
                || !parentSpan.getSpanContext().isValid()
                || parentSpan.getSpanContext().equals(span.getSpanContext());
        long latencyMs = (System.nanoTime() - startTimeNanos) / 1_000_000;

        FinishEventInfo.FinishEventInfoExtra extra =
            new FinishEventInfo.FinishEventInfoExtra(isRootSpan, latencyMs);

        FinishEventInfo info =
            FinishEventInfo.builder()
                .eventType(SpanFinishEvent.SPAN_QUEUE_ENTRY_RATE)
                .isEventFail(false)
                .itemNum(1)
                .extraParams(extra)
                .build();
        finishEventProcessor.process(info);
      } catch (Exception e) {
        logger.warn("FinishEventProcessor error", e);
      }
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
