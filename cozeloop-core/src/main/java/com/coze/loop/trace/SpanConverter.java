package com.coze.loop.trace;

import com.coze.loop.entity.UploadSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Converter to transform OpenTelemetry SpanData to CozeLoop UploadSpan format.
 * 
 * <p>This utility class handles the conversion between OpenTelemetry's span representation
 * ({@link SpanData}) and CozeLoop's span format ({@link UploadSpan}). It extracts and
 * transforms all span data including:
 * <ul>
 *   <li>Basic span information (trace ID, span ID, parent ID, name)</li>
 *   <li>Timing information (start time, duration) - converted from nanoseconds to microseconds</li>
 *   <li>Status information (status code, error state)</li>
 *   <li>Attributes (tags) - categorized by type (string, long, double, boolean)</li>
 *   <li>System tags - special tags prefixed with "system."</li>
 *   <li>CozeLoop-specific attributes (input, output, span type, object storage)</li>
 * </ul>
 * 
 * <p><b>Attribute Mapping Rules:</b>
 * <ul>
 *   <li><b>CozeLoop attributes</b> (prefix "cozeloop."): Extracted to specific UploadSpan fields
 *       <ul>
 *         <li>"cozeloop.input" → {@link UploadSpan#setInput(String)}</li>
 *         <li>"cozeloop.output" → {@link UploadSpan#setOutput(String)}</li>
 *         <li>"cozeloop.object_storage" → {@link UploadSpan#setObjectStorage(String)}</li>
 *         <li>"span.type" → {@link UploadSpan#setSpanType(String)}</li>
 *       </ul>
 *   </li>
 *   <li><b>System tags</b> (prefix "system."): Extracted to system tag maps
 *       <ul>
 *         <li>system.* → systemTagsString, systemTagsLong, systemTagsDouble</li>
 *       </ul>
 *   </li>
 *   <li><b>Regular attributes</b>: Extracted to tag maps by type
 *       <ul>
 *         <li>String → tagsString</li>
 *         <li>Long/Integer → tagsLong</li>
 *         <li>Double → tagsDouble</li>
 *         <li>Boolean → tagsBool</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p><b>Status Code Conversion:</b>
 * <ul>
 *   <li>OpenTelemetry OK → CozeLoop 0</li>
 *   <li>OpenTelemetry ERROR → CozeLoop 1</li>
 *   <li>OpenTelemetry UNSET → CozeLoop 2</li>
 * </ul>
 * 
 * <p><b>Timing Conversion:</b>
 * OpenTelemetry uses nanoseconds for timestamps, while CozeLoop uses microseconds.
 * The converter automatically converts:
 * <ul>
 *   <li>Start time: nanoseconds → microseconds</li>
 *   <li>Duration: calculated as (end - start) in microseconds</li>
 * </ul>
 * 
 * <p><b>Thread Safety:</b>
 * This class is thread-safe. All methods are static and stateless.
 * 
 * <p><b>Usage:</b>
 * <pre>{@code
 * SpanData spanData = ...; // from OpenTelemetry
 * UploadSpan uploadSpan = SpanConverter.convert(spanData, workspaceId, serviceName);
 * // uploadSpan is ready to be sent to CozeLoop platform
 * }</pre>
 * 
 * @see SpanData
 * @see UploadSpan
 * @see CozeLoopSpanExporter
 */
public final class SpanConverter {
    
    private SpanConverter() {
        // Utility class
    }
    
    /**
     * Convert OpenTelemetry SpanData to CozeLoop UploadSpan.
     * 
     * <p>This method performs a complete conversion of OpenTelemetry span data to CozeLoop format.
     * The conversion process:
     * <ol>
     *   <li>Extracts basic span information (IDs, names, timing)</li>
     *   <li>Converts timing from nanoseconds to microseconds</li>
     *   <li>Extracts and categorizes attributes by type</li>
     *   <li>Maps CozeLoop-specific attributes to dedicated fields</li>
     *   <li>Separates system tags from regular tags</li>
     * </ol>
     * 
     * <p><b>Attribute Processing:</b>
     * Attributes are processed in the following order:
     * <ol>
     *   <li>CozeLoop-specific attributes are extracted first (cozeloop.*)</li>
     *   <li>System tags are identified by "system." prefix</li>
     *   <li>Regular attributes are categorized by type (String, Long, Double, Boolean)</li>
     *   <li>Integer values are converted to Long</li>
     * </ol>
     * 
     * <p><b>Null Handling:</b>
     * <ul>
     *   <li>Null attributes are skipped</li>
     *   <li>Empty tag maps are not set on UploadSpan (to reduce payload size)</li>
     *   <li>Null span type defaults to "custom"</li>
     * </ul>
     *
     * @param spanData the OpenTelemetry span data to convert
     * @param workspaceId the CozeLoop workspace ID (added to UploadSpan)
     * @param serviceName the service name (added to UploadSpan)
     * @return converted UploadSpan ready for export to CozeLoop platform
     * @throws NullPointerException if spanData is null
     */
    public static UploadSpan convert(SpanData spanData, String workspaceId, String serviceName) {
        UploadSpan uploadSpan = new UploadSpan();
        
        // Step 1: Extract basic span information
        // These fields directly map from OpenTelemetry to CozeLoop format
        uploadSpan.setTraceId(spanData.getTraceId());
        uploadSpan.setSpanId(spanData.getSpanId());
        uploadSpan.setParentId(spanData.getParentSpanId());
        uploadSpan.setLogId(spanData.getSpanId()); // Use span ID as log ID
        uploadSpan.setWorkspaceId(workspaceId);
        uploadSpan.setServiceName(serviceName);
        
        // Step 2: Convert timing information
        // OpenTelemetry uses nanoseconds (epochNanos), CozeLoop uses microseconds
        // Conversion: 1 microsecond = 1000 nanoseconds
        long startMicros = TimeUnit.NANOSECONDS.toMicros(spanData.getStartEpochNanos());
        long endMicros = TimeUnit.NANOSECONDS.toMicros(spanData.getEndEpochNanos());
        uploadSpan.setStartedAtMicros(startMicros);
        uploadSpan.setDurationMicros(endMicros - startMicros);
        
        // Step 3: Extract span name and type
        uploadSpan.setSpanName(spanData.getName());
        
        // Step 4: Get all attributes from the span
        Attributes attributes = spanData.getAttributes();
        
        // Step 5: Extract span type (defaults to "custom" if not specified)
        String spanType = attributes.get(AttributeKey.stringKey("span.type"));
        uploadSpan.setSpanType(spanType != null ? spanType : "custom");
        
        // Step 6: Convert status code
        // OpenTelemetry: OK, ERROR, UNSET
        // CozeLoop: 0 (OK), 1 (ERROR), 2 (UNSET)
        int statusCode = convertStatusCode(spanData.getStatus().getStatusCode());
        uploadSpan.setStatusCode(statusCode);
        
        // Step 7: Extract CozeLoop-specific attributes
        // These are special attributes that map to dedicated UploadSpan fields
        String input = attributes.get(AttributeKey.stringKey("cozeloop.input"));
        String output = attributes.get(AttributeKey.stringKey("cozeloop.output"));
        uploadSpan.setInput(input);
        uploadSpan.setOutput(output);
        
        // Step 8: Extract object storage key (for multimodal content)
        // This is set by FileUploader when files are uploaded
        String objectStorage = attributes.get(AttributeKey.stringKey("cozeloop.object_storage"));
        uploadSpan.setObjectStorage(objectStorage);
        
        // Step 9: Extract and categorize attributes by type
        // Attributes are separated into:
        // - Regular tags (by type: String, Long, Double, Boolean)
        // - System tags (prefixed with "system.")
        // - CozeLoop attributes are already extracted above
        Map<String, String> tagsString = new HashMap<>();
        Map<String, Long> tagsLong = new HashMap<>();
        Map<String, Double> tagsDouble = new HashMap<>();
        Map<String, Boolean> tagsBool = new HashMap<>();
        Map<String, String> systemTagsString = new HashMap<>();
        Map<String, Long> systemTagsLong = new HashMap<>();
        Map<String, Double> systemTagsDouble = new HashMap<>();
        
        // Iterate through all attributes and categorize them
        attributes.forEach((key, value) -> {
            String keyStr = key.getKey();
            
            // Skip CozeLoop-specific attributes (already extracted above)
            if (keyStr.startsWith("cozeloop.")) {
                return;
            }
            
            // Check if this is a system tag (prefixed with "system.")
            boolean isSystemTag = keyStr.startsWith("system.");
            
            // Categorize by value type
            if (value instanceof String) {
                if (isSystemTag) {
                    systemTagsString.put(keyStr, (String) value);
                } else {
                    tagsString.put(keyStr, (String) value);
                }
            } else if (value instanceof Long) {
                if (isSystemTag) {
                    systemTagsLong.put(keyStr, (Long) value);
                } else {
                    tagsLong.put(keyStr, (Long) value);
                }
            } else if (value instanceof Double) {
                if (isSystemTag) {
                    systemTagsDouble.put(keyStr, (Double) value);
                } else {
                    tagsDouble.put(keyStr, (Double) value);
                }
            } else if (value instanceof Boolean) {
                // Boolean values are only stored as regular tags (not system tags)
                if (!isSystemTag) {
                    tagsBool.put(keyStr, (Boolean) value);
                }
            } else if (value instanceof Integer) {
                // Convert Integer to Long for consistency
                long longValue = ((Integer) value).longValue();
                if (isSystemTag) {
                    systemTagsLong.put(keyStr, longValue);
                } else {
                    tagsLong.put(keyStr, longValue);
                }
            }
            // Note: Other types (Float, etc.) are not currently supported
            // They would need to be converted to supported types
        });
        
        // Step 10: Set tag maps on UploadSpan (only if non-empty to reduce payload size)
        
        if (!tagsString.isEmpty()) {
            uploadSpan.setTagsString(tagsString);
        }
        if (!tagsLong.isEmpty()) {
            uploadSpan.setTagsLong(tagsLong);
        }
        if (!tagsDouble.isEmpty()) {
            uploadSpan.setTagsDouble(tagsDouble);
        }
        if (!tagsBool.isEmpty()) {
            uploadSpan.setTagsBool(tagsBool);
        }
        if (!systemTagsString.isEmpty()) {
            uploadSpan.setSystemTagsString(systemTagsString);
        }
        if (!systemTagsLong.isEmpty()) {
            uploadSpan.setSystemTagsLong(systemTagsLong);
        }
        if (!systemTagsDouble.isEmpty()) {
            uploadSpan.setSystemTagsDouble(systemTagsDouble);
        }
        
        return uploadSpan;
    }
    
    /**
     * Convert OpenTelemetry StatusCode to CozeLoop status code.
     * 
     * <p>OpenTelemetry uses enum values (OK, ERROR, UNSET), while CozeLoop uses
     * integer codes. This method performs the mapping:
     * <ul>
     *   <li>OK → 0 (success)</li>
     *   <li>ERROR → 1 (error occurred)</li>
     *   <li>UNSET → 2 (status not set)</li>
     * </ul>
     * 
     * <p>The status code indicates the outcome of the span:
     * <ul>
     *   <li><b>0 (OK)</b>: Span completed successfully</li>
     *   <li><b>1 (ERROR)</b>: Span ended with an error</li>
     *   <li><b>2 (UNSET)</b>: Status was not explicitly set</li>
     * </ul>
     *
     * @param statusCode the OpenTelemetry StatusCode to convert
     * @return the corresponding CozeLoop status code (0, 1, or 2)
     */
    private static int convertStatusCode(StatusCode statusCode) {
        switch (statusCode) {
            case OK:
                return 0; // Success
            case ERROR:
                return 1; // Error occurred
            default:
                return 2; // UNSET - status not explicitly set
        }
    }
}

