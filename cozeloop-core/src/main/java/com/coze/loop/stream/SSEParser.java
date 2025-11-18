package com.coze.loop.stream;

/**
 * Parser interface for converting SSE events into specific types.
 *
 * @param <T> the type to parse SSE events into
 */
public interface SSEParser<T> {
    /**
     * Parse an SSE event into the target type.
     *
     * @param sse the SSE event to parse
     * @return the parsed object
     * @throws Exception if parsing fails
     */
    T parse(ServerSentEvent sse) throws Exception;
    
    /**
     * Handle error events from SSE stream.
     *
     * @param sse the SSE event that may contain an error
     * @return an exception if this is an error event, null otherwise
     */
    Exception handleError(ServerSentEvent sse);
}

