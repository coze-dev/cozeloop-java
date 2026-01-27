package com.coze.loop.trace;

import com.coze.loop.internal.JsonUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.Map;

/**
 * Wrapper for OpenTelemetry Span that provides CozeLoop-specific methods.
 * 
 * <p>This class wraps OpenTelemetry's {@link Span} and provides:
 * <ul>
 *   <li>CozeLoop-specific convenience methods (setInput, setOutput, setModel, etc.)</li>
 *   <li>Automatic scope management via try-with-resources</li>
 *   <li>Access to underlying OpenTelemetry Span for advanced usage</li>
 * </ul>
 * 
 * <p>The span is automatically made current in the OpenTelemetry context when created,
 * allowing child spans to automatically inherit the parent context.
 * 
 * <p>Example usage:
 * <pre>{@code
 * try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
 *     span.setInput("input data");
 *     span.setOutput("output data");
 *     span.addEvent("important-event");
 * }
 * }</pre>
 * 
 * <p>For advanced OpenTelemetry features, you can access the underlying Span:
 * <pre>{@code
 * CozeLoopSpan cozeSpan = client.startSpan("operation", "custom");
 * Span otelSpan = cozeSpan.getSpan();
 * // Use OpenTelemetry APIs directly
 * }</pre>
 */
public class CozeLoopSpan implements AutoCloseable {
    private final Span span;
    private final Scope scope;
    
    /**
     * Create a new CozeLoopSpan wrapper.
     * 
     * <p>The span is automatically made current in the OpenTelemetry context,
     * which enables automatic context propagation to child spans.
     *
     * @param span the underlying OpenTelemetry Span
     * @param scope the scope that makes this span current in the context
     */
    public CozeLoopSpan(Span span, Scope scope) {
        this.span = span;
        this.scope = scope;
    }
    
    /**
     * Set the input for this span.
     *
     * @param input the input object
     * @return this span
     */
    public CozeLoopSpan setInput(Object input) {
        if (input != null) {
            String inputStr = input instanceof String ? 
                (String) input : JsonUtils.toJson(input);
            span.setAttribute(AttributeKey.stringKey("cozeloop.input"), inputStr);
        }
        return this;
    }
    
    /**
     * Set the output for this span.
     *
     * @param output the output object
     * @return this span
     */
    public CozeLoopSpan setOutput(Object output) {
        if (output != null) {
            String outputStr = output instanceof String ? 
                (String) output : JsonUtils.toJson(output);
            span.setAttribute(AttributeKey.stringKey("cozeloop.output"), outputStr);
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
            span.setStatus(StatusCode.ERROR, error.getMessage());
            span.recordException(error);
        }
        return this;
    }
    
    /**
     * Set status code (0=OK, 1=ERROR).
     *
     * @param statusCode the status code
     * @return this span
     */
    public CozeLoopSpan setStatusCode(int statusCode) {
        if (statusCode == 0) {
            span.setStatus(StatusCode.OK);
        } else {
            span.setStatus(StatusCode.ERROR);
        }
        return this;
    }
    
    /**
     * Set model provider (e.g., "openai", "anthropic").
     *
     * @param provider the model provider
     * @return this span
     */
    public CozeLoopSpan setModelProvider(String provider) {
        if (provider != null) {
            span.setAttribute(AttributeKey.stringKey("llm.provider"), provider);
        }
        return this;
    }
    
    /**
     * Set model name.
     *
     * @param model the model name
     * @return this span
     */
    public CozeLoopSpan setModel(String model) {
        if (model != null) {
            span.setAttribute(AttributeKey.stringKey("llm.model"), model);
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
        span.setAttribute(AttributeKey.longKey("llm.input_tokens"), tokens);
        return this;
    }
    
    /**
     * Set output tokens.
     *
     * @param tokens the number of output tokens
     * @return this span
     */
    public CozeLoopSpan setOutputTokens(long tokens) {
        span.setAttribute(AttributeKey.longKey("llm.output_tokens"), tokens);
        return this;
    }
    
    /**
     * Set total tokens.
     *
     * @param tokens the total number of tokens
     * @return this span
     */
    public CozeLoopSpan setTotalTokens(long tokens) {
        span.setAttribute(AttributeKey.longKey("llm.total_tokens"), tokens);
        return this;
    }
    
    /**
     * Set custom attribute (string).
     *
     * @param key the attribute key
     * @param value the attribute value
     * @return this span
     */
    public CozeLoopSpan setAttribute(String key, String value) {
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
    public CozeLoopSpan setAttribute(String key, long value) {
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
    public CozeLoopSpan setAttribute(String key, double value) {
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
    public CozeLoopSpan setAttribute(String key, boolean value) {
        if (key != null) {
            span.setAttribute(AttributeKey.booleanKey(key), value);
        }
        return this;
    }
    
    /**
     * Add an event to this span.
     * 
     * <p>Events are timestamped annotations on a span that represent something 
     * that happened during the span's lifetime. They are useful for marking 
     * important milestones or state changes.
     * 
     * <p>Example:
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
     * <p>Events are timestamped annotations on a span. This method allows you to
     * add an event with associated attributes. The attributes are set on the span
     * with an "event." prefix to distinguish them from regular span attributes.
     * 
     * <p>Example:
     * <pre>{@code
     * Map<String, Object> eventAttrs = new HashMap<>();
     * eventAttrs.put("response_length", 150L);
     * eventAttrs.put("model", "gpt-4");
     * span.addEvent("llm-response-received", eventAttrs);
     * }</pre>
     * 
     * <p>For more advanced event attributes, you can use the underlying OpenTelemetry Span:
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
     * <p>This is a convenience method that records an exception as an event
     * with exception details. It's equivalent to calling setError() but
     * provides more detailed exception information.
     * 
     * <p>Example:
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
     * <ul>
     *   <li>Current span</li>
     *   <li>Baggage (key-value data propagated across services)</li>
     *   <li>Other context data</li>
     * </ul>
     * 
     * <p>Example:
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
     * <p>The span context contains trace ID, span ID, trace flags, and trace state.
     * It can be used to create links to this span from other spans, or to 
     * propagate trace context across service boundaries.
     * 
     * <p>Example:
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
     * <p>This method provides direct access to the underlying OpenTelemetry Span
     * for advanced use cases that require OpenTelemetry-specific APIs not 
     * exposed through CozeLoopSpan.
     * 
     * <p>Example:
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
     * <p>The scope is what makes this span "current" in the OpenTelemetry context.
     * When the scope is closed, the span is no longer current. This is automatically
     * handled by the try-with-resources pattern, but can be accessed for advanced use cases.
     *
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }
    
    /**
     * End the span and close the scope.
     * 
     * <p>This method is automatically called when using try-with-resources.
     * It ends the span (marking it as finished) and closes the scope (removing
     * it from the current context). The span will then be processed by the
     * BatchSpanProcessor and eventually exported to CozeLoop platform.
     */
    @Override
    public void close() {
        try {
            span.end();
        } finally {
            scope.close();
        }
    }
}

