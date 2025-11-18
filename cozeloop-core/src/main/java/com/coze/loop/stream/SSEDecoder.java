package com.coze.loop.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Decoder for Server-Sent Events (SSE).
 * Parses SSE format from an InputStream.
 */
public class SSEDecoder {
    private static final Logger logger = LoggerFactory.getLogger(SSEDecoder.class);
    
    private final BufferedReader reader;
    
    public SSEDecoder(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
    
    /**
     * Decode a single SSE event from the stream.
     *
     * @return the decoded SSE event, or null if EOF
     * @throws IOException if reading fails
     */
    public ServerSentEvent decodeEvent() throws IOException {
        ServerSentEvent event = new ServerSentEvent();
        List<String> dataLines = new ArrayList<>();
        
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Empty line indicates end of event
            if (line.isEmpty()) {
                if (!dataLines.isEmpty() || event.getEvent() != null || 
                    event.getId() != null || event.getRetry() != null) {
                    event.setData(String.join("\n", dataLines));
                    return event;
                }
                continue;
            }
            
            // Parse field:value format
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                // Line without colon, treat as field name with empty value
                processField(event, line, "", dataLines);
                continue;
            }
            
            String field = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1);
            
            // Remove leading space from value if present
            if (value.startsWith(" ")) {
                value = value.substring(1);
            }
            
            processField(event, field, value, dataLines);
        }
        
        // EOF reached
        if (!dataLines.isEmpty() || event.getEvent() != null || 
            event.getId() != null || event.getRetry() != null) {
            event.setData(String.join("\n", dataLines));
            return event;
        }
        
        return null; // EOF, no more events
    }
    
    /**
     * Process a single SSE field.
     */
    private void processField(ServerSentEvent event, String field, String value, List<String> dataLines) {
        switch (field.toLowerCase()) {
            case "event":
                event.setEvent(value);
                break;
            case "data":
                dataLines.add(value);
                break;
            case "id":
                event.setId(value);
                break;
            case "retry":
                try {
                    event.setRetry(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    logger.debug("Invalid retry value: {}", value);
                }
                break;
            default:
                // Unknown field, ignore
                break;
        }
    }
    
    /**
     * Close the decoder and release resources.
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}

