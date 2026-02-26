package com.coze.loop.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.coze.loop.entity.ExecuteParam;
import com.coze.loop.entity.ExecuteResult;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.http.HttpClient;
import com.coze.loop.internal.Constants;
import com.coze.loop.internal.CozeLoopLogger;
import com.coze.loop.prompt.GetPromptParam;
import com.coze.loop.prompt.PromptProvider;
import com.coze.loop.spec.tracespec.SpanKeys;
import com.coze.loop.stream.StreamReader;
import com.coze.loop.trace.CozeLoopContext;
import com.coze.loop.trace.CozeLoopSpan;
import com.coze.loop.trace.CozeLoopTracerProvider;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

/** Implementation of CozeLoopClient. */
public class CozeLoopClientImpl implements CozeLoopClient {
  private static final Logger logger = CozeLoopLogger.getLogger(CozeLoopClientImpl.class);
  private static final String INSTRUMENTATION_NAME = "cozeloop-java-sdk";
  private static final String INSTRUMENTATION_VERSION = Constants.Version;

  private final String workspaceId;
  private final CozeLoopTracerProvider tracerProvider;
  private final PromptProvider promptProvider;
  private final HttpClient httpClient;
  private final Tracer tracer;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  public CozeLoopClientImpl(
      String workspaceId,
      CozeLoopTracerProvider tracerProvider,
      PromptProvider promptProvider,
      HttpClient httpClient) {
    this.workspaceId = workspaceId;
    this.tracerProvider = tracerProvider;
    this.promptProvider = promptProvider;
    this.httpClient = httpClient;
    this.tracer = tracerProvider.getTracer(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION);

    if (this.promptProvider != null) {
      this.promptProvider.setClient(this);
    }

    logger.info("CozeLoop client initialized for workspace: {}", workspaceId);
  }

  // ========== Trace Operations ==========

  @Override
  public CozeLoopSpan startSpan(String name) {
    return startSpan(name, "custom", (CozeLoopSpan) null);
  }

  @Override
  public CozeLoopSpan startSpan(String name, String spanType) {
    return startSpan(name, spanType, (CozeLoopSpan) null);
  }

  @Override
  public CozeLoopSpan startSpan(String name, String spanType, String scene) {
    return startSpan(name, spanType, (CozeLoopSpan) null, scene);
  }

  @Override
  public CozeLoopSpan startSpan(String name, String spanType, CozeLoopSpan parentSpan) {
    checkNotClosed();

    CozeLoopContext parentContext = parentSpan != null ? parentSpan.getContext() : null;

    return startSpan(name, spanType, parentContext);
  }

  @Override
  public CozeLoopSpan startSpan(
      String name, String spanType, CozeLoopSpan parentSpan, String scene) {
    checkNotClosed();

    CozeLoopContext parentContext = parentSpan != null ? parentSpan.getContext() : null;

    return startSpan(name, spanType, parentContext, scene);
  }

  @Override
  public CozeLoopSpan startSpan(String name, String spanType, CozeLoopContext parentContext) {
    checkNotClosed();

    return startSpan(name, spanType, parentContext, "");
  }

  @Override
  public CozeLoopSpan startSpan(
      String name, String spanType, CozeLoopContext parentContext, String scene) {
    checkNotClosed();

    SpanBuilder spanBuilder = tracer.spanBuilder(name);
    Context contextToUse = parentContext != null ? parentContext : Context.current();

    if (parentContext != null) {
      spanBuilder.setParent(parentContext);
    }

    // Set span type attribute
    spanBuilder.setAttribute(AttributeKey.stringKey(SpanKeys.COZELOOP_SPAN_TYPE), spanType);

    // Auto-apply Baggage from context as Span Attributes (tags), matching Go SDK behavior
    Baggage baggage = Baggage.fromContext(contextToUse);
    baggage.forEach(
        (key, value) -> {
          spanBuilder.setAttribute(AttributeKey.stringKey(key), value.getValue());
        });

    // Start span and make it current in the correct context
    // By using contextToUse.with(span), we ensure that any Baggage in contextToUse
    // (including inherited baggage) is preserved and made current for this thread.
    Span span = spanBuilder.startSpan();
    Context finalContext = contextToUse.with(span);
    Scope scope = finalContext.makeCurrent();

    return new CozeLoopSpan(
        span,
        scope,
        tracerProvider.getPropagators(),
        new CozeLoopContext(finalContext),
        scene,
        tracerProvider.getFinishEventProcessor());
  }

  @Override
  public CozeLoopContext extractContext(Map<String, String> headers) {
    checkNotClosed();
    if (headers == null || headers.isEmpty()) {
      return CozeLoopContext.current();
    }

    return new CozeLoopContext(
        tracerProvider
            .getPropagators()
            .getTextMapPropagator()
            .extract(
                Context.current(),
                headers,
                new TextMapGetter<Map<String, String>>() {
                  @Override
                  public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                  }

                  @Override
                  public String get(Map<String, String> carrier, String key) {
                    return carrier.get(key);
                  }
                }));
  }

  // ========== Prompt Operations ==========

  @Override
  public Prompt getPrompt(GetPromptParam param) {
    checkNotClosed();
    return promptProvider.getPrompt(param);
  }

  @Override
  public List<Message> formatPrompt(Prompt prompt, Map<String, Object> variables) {
    checkNotClosed();
    return promptProvider.formatPrompt(prompt, variables);
  }

  @Override
  public List<Message> getAndFormatPrompt(GetPromptParam param, Map<String, Object> variables) {
    checkNotClosed();
    return promptProvider.getAndFormat(param, variables);
  }

  @Override
  public void invalidatePromptCache(GetPromptParam param) {
    checkNotClosed();
    promptProvider.invalidateCache(param);
  }

  @Override
  public ExecuteResult execute(ExecuteParam param) {
    checkNotClosed();
    return promptProvider.execute(param);
  }

  @Override
  public StreamReader<ExecuteResult> executeStreaming(ExecuteParam param) {
    checkNotClosed();
    return promptProvider.executeStreaming(param);
  }

  // ========== Client Management ==========

  @Override
  public String getWorkspaceId() {
    checkNotClosed();
    return workspaceId;
  }

  @Override
  public void flush() {
    checkNotClosed();
    tracerProvider.flush();
  }

  @Override
  public void shutdown() {
    if (closed.compareAndSet(false, true)) {
      logger.info("Shutting down CozeLoop client");

      try {
        // Shutdown tracer provider (flushes pending spans)
        tracerProvider.shutdown();
      } catch (Exception e) {
        logger.error("Error shutting down tracer provider", e);
      }

      try {
        // Close HTTP client
        httpClient.close();
      } catch (Exception e) {
        logger.error("Error closing HTTP client", e);
      }

      logger.info("CozeLoop client shutdown complete");
    }
  }

  @Override
  public void close() {
    shutdown();
  }

  /** Check if client is closed and throw exception if it is. */
  private void checkNotClosed() {
    if (closed.get()) {
      throw new CozeLoopException(ErrorCode.CLIENT_CLOSED, "CozeLoop client has been closed");
    }
  }
}
