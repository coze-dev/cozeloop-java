package com.coze.loop.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Stream reader for reading typed objects from an SSE stream.
 *
 * @param <T> the type of objects to read
 */
public class StreamReader<T> implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(StreamReader.class);
    
    private final SSEDecoder decoder;
    private final SSEParser<T> parser;
    private boolean closed = false;
    
    public StreamReader(SSEDecoder decoder, SSEParser<T> parser) {
        this.decoder = decoder;
        this.parser = parser;
    }
    
    /**
     * Receive the next item from the stream.
     *
     * @return the next item, or null if stream ended
     * @throws Exception if reading or parsing fails
     */
    public T recv() throws Exception {
        if (closed) {
            throw new IllegalStateException("Stream reader is closed");
        }
        
        while (true) {
            ServerSentEvent sse = decoder.decodeEvent();
            if (sse == null) {
                // Stream ended
                close();
                return null;
            }
            
            // Check for error events first
            Exception error = parser.handleError(sse);
            if (error != null) {
                close();
                throw error;
            }
            
            // Skip empty data
            if (!sse.hasData()) {
                continue;
            }
            
            // Parse the event
            try {
                T result = parser.parse(sse);
                if (result != null) {
                    return result;
                }
                // Continue to next event if parsing returned null
            } catch (Exception e) {
                logger.debug("Failed to parse SSE event, continuing: {}", e.getMessage());
                // Continue to next event for parsing errors
                continue;
            }
        }
    }
    
    /**
     * Check if the stream is closed.
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (decoder != null) {
                decoder.close();
            }
        }
    }
}

