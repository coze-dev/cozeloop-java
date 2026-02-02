package com.coze.loop.client;

import com.coze.loop.entity.ExecuteParam;
import com.coze.loop.entity.ExecuteResult;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.prompt.GetPromptParam;
import com.coze.loop.stream.StreamReader;
import com.coze.loop.trace.CozeLoopSpan;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import java.util.List;
import java.util.Map;

/**
 * Main interface for CozeLoop SDK client.
 * Provides trace and prompt functionality.
 */
public interface CozeLoopClient extends AutoCloseable {
    
    // ========== Trace Operations ==========
    
    /**
     * Start a new span with default type "custom".
     *
     * @param name the span name
     * @return the span wrapper
     */
    CozeLoopSpan startSpan(String name);

    /**
     * Start a new span with specified type.
     *
     * @param name the span name
     * @param spanType the span type (e.g., "llm", "tool", "custom")
     * @return the span wrapper
     */
    CozeLoopSpan startSpan(String name, String spanType);

    CozeLoopSpan startSpan(String name, String spanType, String scene);

    /**
     * Start a new span with specified type.
     *
     * @param name the span name
     * @param spanType the span type (e.g., "llm", "tool", "custom")
     * @return the span wrapper
     */
    CozeLoopSpan startSpan(String name, String spanType, CozeLoopSpan parentSpan);

    CozeLoopSpan startSpan(String name, String spanType, CozeLoopSpan parentSpan, String scene);

    /**
     * Start a new span with specified type and parent context.
     * Useful for cross-service tracing where you have an extracted Context.
     *
     * @param name the span name
     * @param spanType the span type
     * @param parentContext the parent context
     * @return the span wrapper
     */
    CozeLoopSpan startSpan(String name, String spanType, Context parentContext);

    CozeLoopSpan startSpan(String name, String spanType, Context parentContext, String scene);

    /**
     * Extract context from headers for cross-service propagation.
     *
     * @param headers the map of headers (e.g. from an incoming HTTP request)
     * @return the extracted context
     */
    Context extractContext(Map<String, String> headers);
    
    /**
     * Get the underlying OpenTelemetry Tracer.
     *
     * @return tracer instance
     */
    Tracer getTracer();
    
    // ========== Prompt Operations ==========
    
    /**
     * Get a prompt from the platform.
     *
     * @param param the parameters for getting prompt
     * @return prompt
     */
    Prompt getPrompt(GetPromptParam param);
    
    /**
     * Format a prompt with variables.
     *
     * @param prompt the prompt to format
     * @param variables the variables to substitute
     * @return formatted messages
     */
    List<Message> formatPrompt(Prompt prompt, Map<String, Object> variables);
    
    /**
     * Get and format a prompt in one call.
     *
     * @param param the parameters for getting prompt
     * @param variables the variables to substitute
     * @return formatted messages
     */
    List<Message> getAndFormatPrompt(GetPromptParam param, Map<String, Object> variables);
    
    /**
     * Invalidate cached prompt.
     *
     * @param param the parameters identifying the prompt
     */
    void invalidatePromptCache(GetPromptParam param);
    
    /**
     * Execute a prompt.
     *
     * @param param the execution parameters
     * @return the execution result
     */
    ExecuteResult execute(ExecuteParam param);
    
    /**
     * Execute a prompt with streaming response.
     *
     * @param param the execution parameters
     * @return stream reader for ExecuteResult
     */
    StreamReader<ExecuteResult> executeStreaming(ExecuteParam param);
    
    // ========== Client Management ==========
    
    /**
     * Get the workspace ID.
     *
     * @return workspace ID
     */
    String getWorkspaceId();

    /**
     * Flush all pending spans to the backend.
     */
    void flush();
    
    /**
     * Shutdown the client and release resources.
     * This method flushes all pending spans and closes HTTP connections.
     */
    void shutdown();
    
    /**
     * Close the client (alias for shutdown).
     */
    @Override
    void close();
}

