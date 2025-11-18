package com.coze.loop.trace;

import com.coze.loop.entity.UploadFile;
import com.coze.loop.entity.UploadSpan;
import com.coze.loop.http.HttpClient;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom OpenTelemetry SpanExporter that exports spans to CozeLoop platform.
 * 
 * <p>This class implements OpenTelemetry's {@link SpanExporter} interface and is responsible for:
 * <ul>
 *   <li>Converting OpenTelemetry {@link SpanData} to CozeLoop {@link UploadSpan} format</li>
 *   <li>Extracting and uploading multimodal files (images, large text) via {@link FileUploader}</li>
 *   <li>Implementing second-level batching: splitting spans into batches of 25 for remote export</li>
 *   <li>Handling export errors gracefully: individual batch failures don't prevent other batches</li>
 * </ul>
 * 
 * <p><b>Two-Level Batching Architecture:</b>
 * <ol>
 *   <li><b>First Level (OpenTelemetry BatchSpanProcessor)</b>: Receives spans from the application,
 *       batches them up to the configured batch size (default: 512), and sends them to this exporter</li>
 *   <li><b>Second Level (This Exporter)</b>: Further splits the received batch into smaller batches
 *       of 25 spans each, then exports each sub-batch to the CozeLoop platform</li>
 * </ol>
 * 
 * <p><b>Why 25 Spans Per Batch?</b>
 * The batch size of 25 is optimized for:
 * <ul>
 *   <li>Network efficiency: Smaller batches reduce payload size and improve reliability</li>
 *   <li>Error isolation: Failures in one batch don't affect other batches</li>
 *   <li>Server processing: CozeLoop platform processes smaller batches more efficiently</li>
 * </ul>
 * 
 * <p><b>Error Handling:</b>
 * <ul>
 *   <li>Individual batch failures are logged but don't stop processing of other batches</li>
 *   <li>Conversion errors (span data to UploadSpan) fail the entire export</li>
 *   <li>File upload errors are handled gracefully (span is still exported without file reference)</li>
 *   <li>Network errors are retried by the HTTP client (see {@link HttpClient})</li>
 * </ul>
 * 
 * <p><b>Multimodal Support:</b>
 * The exporter automatically:
 * <ul>
 *   <li>Extracts file references from span attributes (images, large text)</li>
 *   <li>Uploads files to CozeLoop object storage via {@link FileUploader}</li>
 *   <li>Attaches object storage keys to spans for later retrieval</li>
 * </ul>
 * 
 * <p><b>Thread Safety:</b>
 * This exporter is thread-safe and can be called concurrently from multiple threads.
 * The {@code isShutdown} flag is volatile to ensure proper visibility across threads.
 * 
 * <p><b>Example Flow:</b>
 * <pre>{@code
 * // 1. OpenTelemetry BatchSpanProcessor sends 100 spans to export()
 * // 2. Exporter converts all 100 spans to UploadSpan format
 * // 3. Exporter splits into 4 batches of 25 spans each
 * // 4. Each batch is exported independently to CozeLoop platform
 * // 5. Results are logged and aggregated
 * }</pre>
 * 
 * @see SpanExporter
 * @see SpanConverter
 * @see FileUploader
 * @see <a href="https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-exporter">OpenTelemetry SpanExporter Specification</a>
 */
public class CozeLoopSpanExporter implements SpanExporter {
    private static final Logger logger = LoggerFactory.getLogger(CozeLoopSpanExporter.class);
    
    /**
     * Batch size for exporting spans to remote server.
     * 
     * <p>Each batch contains at most 25 spans. This is the second-level batch size
     * (the first level is handled by OpenTelemetry's BatchSpanProcessor).
     * 
     * <p>This value is chosen to balance:
     * <ul>
     *   <li>Network efficiency (smaller payloads)</li>
     *   <li>Error isolation (failures don't affect too many spans)</li>
     *   <li>Server processing capacity</li>
     * </ul>
     */
    private static final int EXPORT_BATCH_SIZE = 25;
    
    private final HttpClient httpClient;
    private final String spanEndpoint;
    @SuppressWarnings("unused") // Used by FileUploader constructor
    private final String fileEndpoint;
    private final String workspaceId;
    private final String serviceName;
    private final FileUploader fileUploader;
    
    private volatile boolean isShutdown = false;
    
    /**
     * Create a new CozeLoopSpanExporter.
     * 
     * <p>This constructor initializes the exporter with the necessary components:
     * <ul>
     *   <li>HTTP client for API calls</li>
     *   <li>Endpoints for span and file uploads</li>
     *   <li>Workspace and service identification</li>
     *   <li>File uploader for multimodal content</li>
     * </ul>
     *
     * @param httpClient the HTTP client for making API calls (handles retries, auth, etc.)
     * @param spanEndpoint the CozeLoop API endpoint for uploading spans
     * @param fileEndpoint the CozeLoop API endpoint for uploading files (multimodal content)
     * @param workspaceId the CozeLoop workspace ID
     * @param serviceName the service name (used for resource identification)
     */
    public CozeLoopSpanExporter(HttpClient httpClient,
                                String spanEndpoint,
                                String fileEndpoint,
                                String workspaceId,
                                String serviceName) {
        this.httpClient = httpClient;
        this.spanEndpoint = spanEndpoint;
        this.fileEndpoint = fileEndpoint;
        this.workspaceId = workspaceId;
        this.serviceName = serviceName;
        this.fileUploader = new FileUploader(httpClient, fileEndpoint, workspaceId);
    }
    
    /**
     * Export spans to CozeLoop platform.
     * 
     * <p>This method is called by OpenTelemetry's BatchSpanProcessor with a batch of spans.
     * The implementation:
     * <ol>
     *   <li>Converts all OpenTelemetry SpanData to CozeLoop UploadSpan format</li>
     *   <li>Extracts and uploads multimodal files (if any)</li>
     *   <li>Splits the batch into sub-batches of 25 spans</li>
     *   <li>Exports each sub-batch independently to the remote server</li>
     *   <li>Handles errors gracefully (one batch failure doesn't stop others)</li>
     * </ol>
     * 
     * <p><b>Error Handling Strategy:</b>
     * <ul>
     *   <li>If conversion fails for any span, the entire export fails (returns failure)</li>
     *   <li>If a sub-batch export fails, other sub-batches continue processing</li>
     *   <li>If all sub-batches succeed, returns success</li>
     *   <li>If any sub-batch fails, returns failure (but all successful batches are still exported)</li>
     * </ul>
     * 
     * <p><b>Performance Considerations:</b>
     * <ul>
     *   <li>File extraction and upload happen synchronously (may block briefly)</li>
     *   <li>Batch splitting is O(n) where n is the number of spans</li>
     *   <li>Network calls are made sequentially (one batch at a time)</li>
     * </ul>
     * 
     * @param spans the collection of spans to export (from OpenTelemetry BatchSpanProcessor)
     * @return CompletableResultCode indicating success or failure
     */
    @Override
    public CompletableResultCode export(@javax.annotation.Nonnull Collection<SpanData> spans) {
        // Check if exporter is shutdown
        if (isShutdown) {
            logger.warn("Export called after shutdown, ignoring");
            return CompletableResultCode.ofFailure();
        }
        
        // Handle empty collections
        if (spans == null || spans.isEmpty()) {
            return CompletableResultCode.ofSuccess();
        }
        
        // Step 1: Convert all spans to UploadSpan format
        // This conversion extracts all span data (attributes, events, timing, etc.)
        // and transforms it into CozeLoop's UploadSpan format
        List<UploadSpan> uploadSpans = new ArrayList<>();
        try {
            for (SpanData spanData : spans) {
                // Convert OpenTelemetry SpanData to CozeLoop UploadSpan
                UploadSpan uploadSpan = SpanConverter.convert(spanData, workspaceId, serviceName);
                
                // Step 2: Handle multimodal content (images, large text)
                // Extract file references from span attributes and upload them
                List<UploadFile> files = fileUploader.extractFiles(spanData);
                if (!files.isEmpty()) {
                    // Upload files to CozeLoop object storage
                    String objectStorage = fileUploader.uploadFiles(files);
                    if (objectStorage != null) {
                        // Attach object storage key to span for later retrieval
                        uploadSpan.setObjectStorage(objectStorage);
                    }
                }
                
                uploadSpans.add(uploadSpan);
            }
        } catch (Exception e) {
            // Conversion errors fail the entire export
            logger.error("Failed to convert spans for export", e);
            return CompletableResultCode.ofFailure();
        }
        
        if (uploadSpans.isEmpty()) {
            return CompletableResultCode.ofSuccess();
        }
        
        // Step 3: Split into batches of EXPORT_BATCH_SIZE (25 spans each)
        int totalSpans = uploadSpans.size();
        int totalBatches = (totalSpans + EXPORT_BATCH_SIZE - 1) / EXPORT_BATCH_SIZE;
        int successCount = 0;
        int failureCount = 0;
        
        logger.debug("Exporting {} spans in {} batches (batch size: {})", 
            totalSpans, totalBatches, EXPORT_BATCH_SIZE);
        
        // Step 4: Export each batch independently
        for (int i = 0; i < totalBatches; i++) {
            int start = i * EXPORT_BATCH_SIZE;
            int end = Math.min(start + EXPORT_BATCH_SIZE, totalSpans);
            List<UploadSpan> batch = uploadSpans.subList(start, end);
            
            try {
                // Export this batch to CozeLoop platform
                exportBatch(batch, i + 1, totalBatches);
                successCount++;
                logger.debug("Successfully exported batch {}/{} ({} spans)", 
                    i + 1, totalBatches, batch.size());
            } catch (Exception e) {
                // Individual batch failures don't stop other batches
                failureCount++;
                logger.error("Failed to export batch {}/{} ({} spans): {}", 
                    i + 1, totalBatches, batch.size(), e.getMessage(), e);
                // Continue processing other batches even if one fails
            }
        }
        
        // Step 5: Return result based on batch outcomes
        if (failureCount == 0) {
            logger.debug("Successfully exported all {} spans in {} batches", 
                totalSpans, totalBatches);
            return CompletableResultCode.ofSuccess();
        } else {
            logger.warn("Exported {} spans: {} batches succeeded, {} batches failed", 
                totalSpans, successCount, failureCount);
            // Return failure if any batch failed (but successful batches are still exported)
            return CompletableResultCode.ofFailure();
        }
    }
    
    /**
     * Export a single batch of spans to the remote server.
     * 
     * <p>This method sends a batch of UploadSpan objects to the CozeLoop platform
     * via HTTP POST. The payload is a JSON object containing the spans array.
     * 
     * <p><b>Payload Format:</b>
     * <pre>{@code
     * {
     *   "spans": [
     *     { ... },
     *     { ... },
     *     ...
     *   ]
     * }
     * }</pre>
     * 
     * <p><b>Error Handling:</b>
     * Exceptions thrown by this method are caught by the caller ({@link #export}),
     * which logs the error and continues processing other batches.
     * 
     * <p><b>Network Retries:</b>
     * The HTTP client handles retries automatically (see {@link HttpClient}).
     * This method will only throw an exception if all retries are exhausted.
     *
     * @param batch the batch of upload spans to export (typically 25 spans)
     * @param batchNumber the batch number (1-based, for logging purposes)
     * @param totalBatches the total number of batches (for logging purposes)
     * @throws Exception if the HTTP request fails after all retries
     */
    private void exportBatch(List<UploadSpan> batch, int batchNumber, int totalBatches) throws Exception {
        // Build payload: { "spans": [ ... ] }
        Map<String, Object> payload = new HashMap<>();
        payload.put("spans", batch);
        
        // Send HTTP POST request to CozeLoop platform
        // The HTTP client handles authentication, retries, and error handling
        httpClient.post(spanEndpoint, payload);
        
        // Log at trace level for detailed debugging
        if (logger.isTraceEnabled()) {
            logger.trace("Exported batch {}/{} with {} spans to CozeLoop", 
                batchNumber, totalBatches, batch.size());
        }
    }
    
    /**
     * Flush any pending spans.
     * 
     * <p>This exporter doesn't buffer spans internally (all buffering is handled
     * by OpenTelemetry's BatchSpanProcessor), so this method is a no-op.
     * 
     * <p>When called, it immediately returns success since there's nothing to flush.
     * 
     * @return always returns success (no buffering to flush)
     */
    @Override
    public CompletableResultCode flush() {
        // No buffering in this exporter, so flush is a no-op
        // All buffering is handled by OpenTelemetry's BatchSpanProcessor
        return CompletableResultCode.ofSuccess();
    }
    
    /**
     * Shutdown the exporter and release resources.
     * 
     * <p>This method should be called when the application is shutting down to:
     * <ul>
     *   <li>Mark the exporter as shutdown (prevents new exports)</li>
     *   <li>Close HTTP client connections</li>
     *   <li>Release any held resources</li>
     * </ul>
     * 
     * <p><b>Important:</b> After shutdown, the exporter cannot be used again.
     * Any calls to {@link #export} after shutdown will return failure.
     * 
     * <p>This method is idempotent: calling it multiple times is safe.
     * 
     * @return CompletableResultCode indicating success or failure
     */
    @Override
    public CompletableResultCode shutdown() {
        if (isShutdown) {
            // Already shutdown, return success (idempotent)
            return CompletableResultCode.ofSuccess();
        }
        
        isShutdown = true;
        
        try {
            // Close HTTP client to release connections and resources
            httpClient.close();
            logger.info("CozeLoopSpanExporter shutdown completed");
            return CompletableResultCode.ofSuccess();
        } catch (Exception e) {
            logger.error("Failed to shutdown exporter", e);
            return CompletableResultCode.ofFailure();
        }
    }
}

