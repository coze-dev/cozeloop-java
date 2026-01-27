package com.coze.loop.trace;

import com.coze.loop.http.HttpClient;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * CozeLoop TracerProvider that wraps OpenTelemetry TracerProvider.
 * 
 * <p>This class provides a bridge between CozeLoop SDK and OpenTelemetry, managing
 * the complete trace lifecycle from span creation to export. It configures and
 * initializes the OpenTelemetry SDK with CozeLoop-specific exporters and processors.
 * 
 * <p><b>Architecture Overview:</b>
 * <ul>
 *   <li><b>Resource</b>: Defines service metadata (service name, workspace ID)</li>
 *   <li><b>SdkTracerProvider</b>: Manages Tracer instances and SpanProcessors</li>
 *   <li><b>BatchSpanProcessor</b>: First-level batching (configurable batch size)</li>
 *   <li><b>CozeLoopSpanExporter</b>: Second-level batching (25 spans per batch) and export</li>
 * </ul>
 * 
 * <p><b>Two-Level Batching Strategy:</b>
 * <ol>
 *   <li><b>OpenTelemetry BatchSpanProcessor</b>: Batches spans up to the configured
 *       batch size (default: 512) or until the schedule delay expires (default: 5000ms)</li>
 *   <li><b>CozeLoopSpanExporter</b>: Further splits batches into groups of 25 spans
 *       for efficient remote server export</li>
 * </ol>
 * 
 * <p><b>Context Propagation:</b>
 * The TracerProvider automatically handles OpenTelemetry context propagation,
 * ensuring that trace context (trace ID, span ID, baggage) is automatically
 * propagated to child spans within the same thread and across async boundaries.
 * 
 * <p><b>Example Usage:</b>
 * <pre>{@code
 * TraceConfig config = TraceConfig.builder()
 *     .maxQueueSize(2048)
 *     .batchSize(512)
 *     .scheduleDelayMillis(5000)
 *     .exportTimeoutMillis(30000)
 *     .build();
 * 
 * CozeLoopTracerProvider provider = CozeLoopTracerProvider.create(
 *     httpClient, spanEndpoint, fileEndpoint, workspaceId, serviceName, config);
 * 
 * Tracer tracer = provider.getTracer("my-instrumentation");
 * }</pre>
 * 
 * <p><b>Shutdown:</b>
 * Always call {@link #shutdown()} when the application is shutting down to ensure
 * all pending spans are flushed and exported:
 * <pre>{@code
 * provider.shutdown(); // Flushes and exports all pending spans
 * }</pre>
 * 
 * @see <a href="https://opentelemetry.io/docs/instrumentation/java/">OpenTelemetry Java Documentation</a>
 * @see CozeLoopSpanExporter
 * @see BatchSpanProcessor
 */
public class CozeLoopTracerProvider {
    private static final Logger logger = LoggerFactory.getLogger(CozeLoopTracerProvider.class);
    
    private final SdkTracerProvider sdkTracerProvider;
    private final OpenTelemetrySdk openTelemetrySdk;
    private final CozeLoopSpanExporter spanExporter;
    
    /**
     * Private constructor. Use {@link #create} to create instances.
     * 
     * <p>This constructor initializes the OpenTelemetry SDK with:
     * <ol>
     *   <li><b>CozeLoopSpanExporter</b>: Custom exporter that converts and exports spans
     *       to CozeLoop platform in batches of 25</li>
     *   <li><b>Resource</b>: Service metadata including service name and workspace ID</li>
     *   <li><b>BatchSpanProcessor</b>: First-level batching processor with configurable
     *       queue size, batch size, and timing</li>
     *   <li><b>SdkTracerProvider</b>: OpenTelemetry SDK tracer provider that manages
     *       the complete trace pipeline</li>
     * </ol>
     * 
     * <p>The OpenTelemetry SDK is registered globally if not already initialized,
     * allowing it to work seamlessly with other OpenTelemetry instrumentation.
     *
     * @param httpClient the HTTP client for API calls
     * @param spanEndpoint the endpoint for uploading spans
     * @param fileEndpoint the endpoint for uploading files (multimodal content)
     * @param workspaceId the CozeLoop workspace ID
     * @param serviceName the service name for resource identification
     * @param config the trace configuration (batch sizes, timeouts, etc.)
     */
    private CozeLoopTracerProvider(HttpClient httpClient,
                                    String spanEndpoint,
                                    String fileEndpoint,
                                    String workspaceId,
                                    String serviceName,
                                    TraceConfig config) {
        // Step 1: Create the custom span exporter
        // This exporter implements OpenTelemetry's SpanExporter interface and handles
        // conversion from OpenTelemetry SpanData to CozeLoop format, plus second-level batching
        this.spanExporter = new CozeLoopSpanExporter(
            httpClient, spanEndpoint, fileEndpoint, workspaceId, serviceName);
        
        // Step 2: Create Resource with service metadata
        // Resource attributes are attached to all spans and help identify the service
        // in the CozeLoop platform. These attributes are part of OpenTelemetry's
        // resource model and are automatically included in all exported spans.
        Resource resource = Resource.getDefault()
            .merge(Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put("workspace.id", workspaceId)
                .build());
        
        // Step 3: Create BatchSpanProcessor (first-level batching)
        // This processor:
        // - Queues spans up to maxQueueSize
        // - Batches spans up to batchSize before sending to exporter
        // - Exports on schedule (scheduleDelay) or when batch is full
        // - Uses async processing to avoid blocking application threads
        BatchSpanProcessor batchProcessor = BatchSpanProcessor.builder(spanExporter)
            .setMaxQueueSize(config.getMaxQueueSize())
            .setMaxExportBatchSize(config.getBatchSize())
            .setScheduleDelay(config.getScheduleDelayMillis(), TimeUnit.MILLISECONDS)
            .setExporterTimeout(config.getExportTimeoutMillis(), TimeUnit.MILLISECONDS)
            .build();
        
        // Step 4: Create SdkTracerProvider
        // This is the core OpenTelemetry component that:
        // - Manages Tracer instances
        // - Processes spans through SpanProcessors
        // - Attaches Resource attributes to all spans
        this.sdkTracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(batchProcessor)
            .build();
        
        // Step 5: Build and register OpenTelemetry SDK
        // We check if GlobalOpenTelemetry is already set to avoid conflicts in tests
        // or when multiple OpenTelemetry instances are used in the same application.
        OpenTelemetrySdk sdk;
        try {
            // Try to get existing instance - if this succeeds, it's already set
            GlobalOpenTelemetry.get();
            // Already set, build without registering globally to avoid conflicts
            sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .build();
            logger.debug("OpenTelemetry already initialized globally, using non-global instance");
        } catch (IllegalStateException e) {
            // Not set yet, register globally (normal case)
            // This makes the TracerProvider available via GlobalOpenTelemetry.get()
            sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();
        }
        this.openTelemetrySdk = sdk;
        
        logger.info("CozeLoop TracerProvider initialized with service: {}, workspace: {}",
            serviceName, workspaceId);
    }
    
    /**
     * Create a new CozeLoopTracerProvider.
     *
     * @param httpClient the HTTP client
     * @param spanEndpoint the span upload endpoint
     * @param fileEndpoint the file upload endpoint
     * @param workspaceId the workspace ID
     * @param serviceName the service name
     * @param config the trace configuration
     * @return CozeLoopTracerProvider instance
     */
    public static CozeLoopTracerProvider create(HttpClient httpClient,
                                                 String spanEndpoint,
                                                 String fileEndpoint,
                                                 String workspaceId,
                                                 String serviceName,
                                                 TraceConfig config) {
        return new CozeLoopTracerProvider(
            httpClient, spanEndpoint, fileEndpoint, workspaceId, serviceName, config);
    }
    
    /**
     * Get a Tracer for creating spans.
     * 
     * <p>The instrumentation name identifies the library or framework that is
     * creating spans. This helps organize spans in the CozeLoop platform.
     * 
     * <p>Example:
     * <pre>{@code
     * Tracer tracer = provider.getTracer("my-application", "1.0.0");
     * Span span = tracer.spanBuilder("operation").startSpan();
     * }</pre>
     *
     * @param instrumentationName the name of the instrumentation library
     *        (e.g., "cozeloop-java-sdk", "my-application")
     * @return Tracer instance for creating spans
     */
    public Tracer getTracer(String instrumentationName) {
        return openTelemetrySdk.getTracer(instrumentationName);
    }
    
    /**
     * Get the underlying OpenTelemetry TracerProvider.
     * 
     * <p>This provides direct access to the OpenTelemetry TracerProvider for
     * advanced use cases that require OpenTelemetry-specific APIs.
     * 
     * <p>Example:
     * <pre>{@code
     * TracerProvider provider = tracerProvider.getTracerProvider();
     * // Use OpenTelemetry APIs directly
     * }</pre>
     *
     * @return the underlying OpenTelemetry TracerProvider
     */
    public TracerProvider getTracerProvider() {
        return sdkTracerProvider;
    }
    
    /**
     * Shutdown the tracer provider and flush all pending spans.
     * 
     * <p>This method should be called when the application is shutting down to ensure:
     * <ul>
     *   <li>All pending spans are flushed from the queue</li>
     *   <li>All spans are exported to CozeLoop platform</li>
     *   <li>Resources are properly released</li>
     * </ul>
     * 
     * <p><b>Important:</b> After shutdown, the TracerProvider cannot be used again.
     * You must create a new instance if needed.
     * 
     * <p>The shutdown process:
     * <ol>
     *   <li>Force flush all pending spans (waits up to 10 seconds)</li>
     *   <li>Shutdown the TracerProvider (waits up to 10 seconds)</li>
     *   <li>Shutdown the SpanExporter</li>
     * </ol>
     * 
     * <p>Example:
     * <pre>{@code
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     *     tracerProvider.shutdown();
     * }));
     * }</pre>
     */
    public void shutdown() {
        logger.info("Shutting down CozeLoop TracerProvider");
        try {
            // Force flush: ensures all queued spans are processed and exported
            sdkTracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
            // Shutdown: stops accepting new spans and flushes remaining ones
            sdkTracerProvider.shutdown().join(10, TimeUnit.SECONDS);
            // Shutdown exporter: closes HTTP connections and releases resources
            spanExporter.shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down tracer provider", e);
        }
    }
    
    /**
     * Trace configuration for OpenTelemetry BatchSpanProcessor.
     * 
     * <p>This configuration controls how spans are batched and exported:
     * <ul>
     *   <li><b>maxQueueSize</b>: Maximum number of spans that can be queued
     *       before spans are dropped (default: 2048)</li>
     *   <li><b>batchSize</b>: Maximum number of spans per batch sent to exporter
     *       (default: 512). Note: CozeLoopSpanExporter further splits into batches of 25</li>
     *   <li><b>scheduleDelayMillis</b>: Time between automatic batch exports
     *       (default: 5000ms = 5 seconds)</li>
     *   <li><b>exportTimeoutMillis</b>: Maximum time to wait for export to complete
     *       (default: 30000ms = 30 seconds)</li>
     * </ul>
     * 
     * <p><b>Tuning Guidelines:</b>
     * <ul>
     *   <li><b>High Throughput</b>: Increase maxQueueSize and batchSize</li>
     *   <li><b>Low Latency</b>: Decrease scheduleDelayMillis</li>
     *   <li><b>Network Issues</b>: Increase exportTimeoutMillis</li>
     * </ul>
     */
    public static class TraceConfig {
        /** Maximum number of spans in the queue before dropping (default: 2048) */
        private int maxQueueSize = 2048;
        
        /** Maximum spans per batch sent to exporter (default: 512) */
        private int batchSize = 512;
        
        /** Delay between automatic batch exports in milliseconds (default: 5000) */
        private long scheduleDelayMillis = 5000;
        
        /** Timeout for export operations in milliseconds (default: 30000) */
        private long exportTimeoutMillis = 30000;
        
        public int getMaxQueueSize() {
            return maxQueueSize;
        }
        
        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public long getScheduleDelayMillis() {
            return scheduleDelayMillis;
        }
        
        public void setScheduleDelayMillis(long scheduleDelayMillis) {
            this.scheduleDelayMillis = scheduleDelayMillis;
        }
        
        public long getExportTimeoutMillis() {
            return exportTimeoutMillis;
        }
        
        public void setExportTimeoutMillis(long exportTimeoutMillis) {
            this.exportTimeoutMillis = exportTimeoutMillis;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final TraceConfig config = new TraceConfig();
            
            public Builder maxQueueSize(int size) {
                config.maxQueueSize = size;
                return this;
            }
            
            public Builder batchSize(int size) {
                config.batchSize = size;
                return this;
            }
            
            public Builder scheduleDelayMillis(long millis) {
                config.scheduleDelayMillis = millis;
                return this;
            }
            
            public Builder exportTimeoutMillis(long millis) {
                config.exportTimeoutMillis = millis;
                return this;
            }
            
            public TraceConfig build() {
                return config;
            }
        }
    }
}

