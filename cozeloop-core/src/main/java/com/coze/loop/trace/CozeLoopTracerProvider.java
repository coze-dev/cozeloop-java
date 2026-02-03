package com.coze.loop.trace;

import com.coze.loop.auth.Auth;
import com.coze.loop.internal.UserAgentUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * CozeLoop TracerProvider that wraps OpenTelemetry TracerProvider.
 *
 * <p>This class provides a bridge between CozeLoop SDK and OpenTelemetry, managing
 * the complete trace lifecycle from span creation to export. It configures and
 * initializes the OpenTelemetry SDK with OTLP/HTTP exporter and BatchSpanProcessor.
 *
 * <p><b>Architecture Overview:</b>
 * <ul>
 *   <li><b>Resource</b>: Defines service metadata (service name, workspace ID)</li>
 *   <li><b>SdkTracerProvider</b>: Manages Tracer instances and SpanProcessors</li>
 *   <li><b>BatchSpanProcessor</b>: Handles batching (configurable batch size)</li>
 *   <li><b>OtlpHttpSpanExporter</b>: Standard OTLP/HTTP exporter for remote export</li>
 * </ul>
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
 *     spanEndpoint, workspaceId, serviceName, config);
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
 * @see BatchSpanProcessor
 * @see OtlpHttpSpanExporter
 */
public class CozeLoopTracerProvider {
    private static final Logger logger = LoggerFactory.getLogger(CozeLoopTracerProvider.class);

    private final SdkTracerProvider sdkTracerProvider;
    private final OpenTelemetrySdk openTelemetrySdk;
    private final SpanExporter spanExporter;

    /**
     * Private constructor. Use {@link #create} to create instances.
     *
     * <p>This constructor initializes the OpenTelemetry SDK with:
     * <ol>
     *   <li><b>OtlpHttpSpanExporter</b>: Standard OTLP/HTTP exporter</li>
     *   <li><b>Resource</b>: Service metadata including service name and workspace ID</li>
     *   <li><b>BatchSpanProcessor</b>: Batching processor with configurable
     *       queue size, batch size, and timing</li>
     * </ol>
     *
     * @param spanEndpoint the endpoint for uploading spans via OTLP/HTTP
     * @param workspaceId  the CozeLoop workspace ID
     * @param serviceName  the service name
     * @param config       the trace configuration
     */
    private CozeLoopTracerProvider(Auth auth,
                                   String spanEndpoint,
                                   String workspaceId,
                                   String serviceName,
                                   TraceConfig config) {
        // Step 1: Create OtlpHttpSpanExporter
        Supplier<Map<String, String>> authHeaders = () -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("cozeloop-workspace-id", workspaceId);
            headers.put("Authorization", "Bearer " + auth.getToken());
            headers.put("User-Agent", UserAgentUtils.getUserAgent());
            headers.put("X-Coze-Client-User-Agent", UserAgentUtils.getClientUserAgent());

            String ttEnv = System.getenv("x_tt_env");
            if (ttEnv != null && !ttEnv.isEmpty()) {
                headers.put("x-tt-env", ttEnv);
            }
            String usePpe = System.getenv("x_use_ppe");
            if (usePpe!= null &&!usePpe.isEmpty()) {
                headers.put("x-use-ppe", "1");
            }

            return headers;
        };
        this.spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(spanEndpoint)
                .setHeaders(authHeaders)
                .build();

        // Step 2: Create Resource with service metadata
        Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put("service.name", serviceName)
                        .put("workspace.id", workspaceId)
                        .build());

        // Step 3: Create BatchSpanProcessor
        BatchSpanProcessor batchProcessor = BatchSpanProcessor.builder(spanExporter)
                .setMaxQueueSize(config.getMaxQueueSize())
                .setMaxExportBatchSize(config.getBatchSize())
                .setScheduleDelay(config.getScheduleDelayMillis(), TimeUnit.MILLISECONDS)
                .setExporterTimeout(config.getExportTimeoutMillis(), TimeUnit.MILLISECONDS)
                .build();

        // Step 4: Create SdkTracerProvider
        this.sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(batchProcessor)
                .build();

        // Step 5: Build and register OpenTelemetry SDK
        ContextPropagators propagators = ContextPropagators.create(
                TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance()));

        OpenTelemetrySdk sdk;
        try {
            GlobalOpenTelemetry.get();
            sdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(propagators)
                    .build();
            logger.debug("OpenTelemetry already initialized globally, using non-global instance");
        } catch (IllegalStateException e) {
            sdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(sdkTracerProvider)
                    .setPropagators(propagators)
                    .buildAndRegisterGlobal();
        }
        this.openTelemetrySdk = sdk;

        logger.info("CozeLoop TracerProvider initialized with service: {}, workspace: {}, endpoint: {}",
                serviceName, workspaceId, spanEndpoint);
    }

    /**
     * Create a new CozeLoopTracerProvider.
     *
     * @param spanEndpoint the span upload endpoint
     * @param workspaceId  the workspace ID
     * @param serviceName  the service name
     * @param config       the trace configuration
     * @return CozeLoopTracerProvider instance
     */
    public static CozeLoopTracerProvider create(Auth auth,
                                                String spanEndpoint,
                                                String workspaceId,
                                                String serviceName,
                                                TraceConfig config) {
        return new CozeLoopTracerProvider(auth, spanEndpoint, workspaceId, serviceName, config);
    }

    /**
     * Get the ContextPropagators configured for this SDK.
     *
     * @return the context propagators
     */
    public ContextPropagators getPropagators() {
        return openTelemetrySdk.getPropagators();
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
     *                            (e.g., "cozeloop-java-sdk", "my-application")
     * @return Tracer instance for creating spans
     */
    public Tracer getTracer(String instrumentationName, String instrumentationVersion) {
        return openTelemetrySdk.getTracer(instrumentationName, instrumentationVersion);
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
     * Force flush all pending spans.
     * This method waits up to 10 seconds for the flush to complete.
     */
    public void flush() {
        sdkTracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
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
        /**
         * Maximum number of spans in the queue before dropping (default: 2048)
         */
        private int maxQueueSize = 2048;

        /**
         * Maximum spans per batch sent to exporter (default: 512)
         */
        private int batchSize = 512;

        /**
         * Delay between automatic batch exports in milliseconds (default: 5000)
         */
        private long scheduleDelayMillis = 5000;

        /**
         * Timeout for export operations in milliseconds (default: 30000)
         */
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

