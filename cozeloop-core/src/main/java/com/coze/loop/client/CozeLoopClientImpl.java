package com.coze.loop.client;

import com.coze.loop.entity.ExecuteParam;
import com.coze.loop.entity.ExecuteResult;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.http.HttpClient;
import com.coze.loop.prompt.GetPromptParam;
import com.coze.loop.prompt.PromptProvider;
import com.coze.loop.stream.StreamReader;
import com.coze.loop.trace.CozeLoopSpan;
import com.coze.loop.trace.CozeLoopTracerProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of CozeLoopClient.
 */
public class CozeLoopClientImpl implements CozeLoopClient {
    private static final Logger logger = LoggerFactory.getLogger(CozeLoopClientImpl.class);
    private static final String INSTRUMENTATION_NAME = "cozeloop-java-sdk";
    
    private final String workspaceId;
    private final CozeLoopTracerProvider tracerProvider;
    private final PromptProvider promptProvider;
    private final HttpClient httpClient;
    private final Tracer tracer;
    
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    public CozeLoopClientImpl(String workspaceId,
                              CozeLoopTracerProvider tracerProvider,
                              PromptProvider promptProvider,
                              HttpClient httpClient) {
        this.workspaceId = workspaceId;
        this.tracerProvider = tracerProvider;
        this.promptProvider = promptProvider;
        this.httpClient = httpClient;
        this.tracer = tracerProvider.getTracer(INSTRUMENTATION_NAME);
        
        logger.info("CozeLoop client initialized for workspace: {}", workspaceId);
    }
    
    // ========== Trace Operations ==========
    
    @Override
    public CozeLoopSpan startSpan(String name) {
        return startSpan(name, "custom");
    }
    
    @Override
    public CozeLoopSpan startSpan(String name, String spanType) {
        checkNotClosed();
        
        SpanBuilder spanBuilder = tracer.spanBuilder(name);
        
        // Set span type attribute
        spanBuilder.setAttribute(AttributeKey.stringKey("span.type"), spanType);
        
        // Start span and make it current
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();
        
        return new CozeLoopSpan(span, scope);
    }
    
    @Override
    public Tracer getTracer() {
        checkNotClosed();
        return tracer;
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
    
    /**
     * Check if client is closed and throw exception if it is.
     */
    private void checkNotClosed() {
        if (closed.get()) {
            throw new CozeLoopException(ErrorCode.CLIENT_CLOSED,
                "CozeLoop client has been closed");
        }
    }
}

