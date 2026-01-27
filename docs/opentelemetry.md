# OpenTelemetry Integration Guide

## Overview

CozeLoop Java SDK is built on top of [OpenTelemetry](https://opentelemetry.io/), a vendor-neutral observability framework. This document explains how OpenTelemetry is integrated into the SDK and how to use it effectively.

## Why OpenTelemetry?

OpenTelemetry provides:
- **Industry Standard**: Widely adopted observability framework
- **Vendor Neutral**: Works with any backend that supports OpenTelemetry
- **Rich Ecosystem**: Extensive instrumentation libraries
- **Automatic Batching**: Built-in batch processing for efficient export
- **Context Propagation**: Automatic trace context propagation across services
- **Mature & Battle-Tested**: Production-ready with excellent performance

## Architecture

The SDK uses OpenTelemetry's architecture with the following components:

```
┌─────────────────────────────────────────────────────────┐
│              CozeLoop Java SDK                          │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │         CozeLoopClient                            │  │
│  │  (High-level API for users)                       │  │
│  └──────────────┬───────────────────────────────────┘  │
│                 │                                        │
│  ┌──────────────▼───────────────────────────────────┐  │
│  │      CozeLoopTracerProvider                      │  │
│  │  ┌────────────────────────────────────────────┐  │  │
│  │  │  OpenTelemetry SDK                         │  │  │
│  │  │  ┌──────────────────────────────────────┐ │  │  │
│  │  │  │  SdkTracerProvider                   │ │  │  │
│  │  │  │  ┌────────────────────────────────┐ │ │  │  │
│  │  │  │  │  BatchSpanProcessor            │ │ │  │  │
│  │  │  │  │  (First-level batching)        │ │ │  │  │
│  │  │  │  └────────────┬───────────────────┘ │ │  │  │
│  │  │  └───────────────┼───────────────────────┘ │  │  │
│  │  └──────────────────┼─────────────────────────┘  │  │
│  └─────────────────────┼──────────────────────────────┘  │
│                        │                                   │
│  ┌─────────────────────▼──────────────────────────────┐  │
│  │      CozeLoopSpanExporter                          │  │
│  │  (Implements OpenTelemetry SpanExporter)           │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  Second-level batching (25 spans per batch) │  │  │
│  │  └──────────────┬───────────────────────────────┘  │  │
│  └─────────────────┼───────────────────────────────────┘  │
│                    │                                       │
│  ┌─────────────────▼───────────────────────────────────┐  │
│  │         CozeLoop Platform                            │  │
│  │         (Remote Server)                              │  │
│  └──────────────────────────────────────────────────────┘  │
```

## Components

### 1. TracerProvider

`CozeLoopTracerProvider` wraps OpenTelemetry's `SdkTracerProvider` and manages:
- **Resource**: Service metadata (service name, workspace ID)
- **Tracer**: Creates spans for instrumentation
- **SpanProcessor**: Processes and exports spans

### 2. BatchSpanProcessor

OpenTelemetry's `BatchSpanProcessor` provides:
- **Queue Management**: Buffers spans before export
- **Automatic Batching**: Groups spans into batches
- **Scheduled Export**: Exports on schedule or when batch is full
- **Async Processing**: Non-blocking span processing

**Configuration Options:**
- `maxQueueSize`: Maximum number of spans in queue (default: 2048)
- `batchSize`: Maximum spans per batch (default: 512)
- `scheduleDelay`: Time between exports (default: 5000ms)
- `exportTimeout`: Timeout for export operations (default: 30000ms)

### 3. SpanExporter

`CozeLoopSpanExporter` implements OpenTelemetry's `SpanExporter` interface:
- Receives batches of spans from `BatchSpanProcessor`
- Converts OpenTelemetry `SpanData` to CozeLoop format using `SpanConverter`
- Handles file uploads for multimodal content (images, large text)
- Splits into smaller batches of 25 spans for efficient remote export
- Exports to CozeLoop platform via HTTP with error handling

**Two-Level Batching:**
1. **First Level**: OpenTelemetry `BatchSpanProcessor` batches up to 512 spans (configurable)
2. **Second Level**: `CozeLoopSpanExporter` splits into batches of 25 spans for remote server

**Error Handling:**
- Individual batch failures don't prevent other batches from being exported
- Comprehensive logging for monitoring and debugging
- Automatic retry via HTTP client retry mechanism

### 4. Span Wrapper

`CozeLoopSpan` wraps OpenTelemetry's `Span` and provides:
- CozeLoop-specific methods (setInput, setOutput, setModel, etc.)
- Automatic scope management (try-with-resources)
- Direct access to underlying OpenTelemetry Span
- Support for Events (addEvent)
- Support for Links (addLink)
- Full OpenTelemetry attribute support
- Error recording (setError, recordException)

## Context Propagation

OpenTelemetry automatically propagates trace context across:
- **Thread boundaries**: Child spans inherit parent context
- **Service boundaries**: Trace context via HTTP headers (W3C Trace Context)
- **Async operations**: Context preserved in CompletableFuture, ExecutorService

### Example: Context Propagation

```java
// Parent span
try (CozeLoopSpan parentSpan = client.startSpan("parent", "custom")) {
    parentSpan.setAttribute("user_id", "12345");
    
    // Child span automatically inherits parent context
    try (CozeLoopSpan childSpan = client.startSpan("child", "custom")) {
        // This span is automatically linked to parent
        childSpan.setInput("child operation");
    }
    
    // Async operation with context propagation
    CompletableFuture.runAsync(() -> {
        // Context is automatically propagated
        try (CozeLoopSpan asyncSpan = client.startSpan("async", "custom")) {
            // This span is also a child of parent
        }
    });
}
```

## Using OpenTelemetry APIs Directly

You can also use OpenTelemetry APIs directly:

```java
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

// Get the underlying OpenTelemetry Tracer
Tracer tracer = client.getTracer();

// Create span using OpenTelemetry API
Span span = tracer.spanBuilder("my-operation")
    .setAttribute("custom.key", "value")
    .startSpan();

try (Scope scope = span.makeCurrent()) {
    // Your code here
    span.addEvent("event-name");
    span.setStatus(StatusCode.OK);
} finally {
    span.end();
}
```

## Span Lifecycle

1. **Start**: Span is created and made current in context
2. **Active**: Span is in context, child spans inherit it
3. **End**: Span is finished and sent to processor
4. **Processed**: BatchSpanProcessor batches the span
5. **Exported**: CozeLoopSpanExporter converts and sends to platform

## Advanced Features

### Events

Events are timestamped annotations on a span that represent something that happened during the span's lifetime:

```java
try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
    span.addEvent("operation-started");
    // ... do work ...
    span.addEvent("operation-completed");
}
```

### Links

Links connect spans to other spans, typically used to represent causal relationships:

```java
// Get span context from another trace
SpanContext linkedSpanContext = ...;

try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
    span.addLink(linkedSpanContext);
    // ... do work ...
}
```

### Baggage

Baggage is key-value data that is propagated across service boundaries. It's useful for passing contextual information:

```java
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;

// Set baggage in current context
Baggage baggage = Baggage.builder()
    .put("user_id", "12345")
    .put("request_id", "req-abc")
    .build();

try (Scope scope = baggage.storeInContext(Context.current()).makeCurrent()) {
    // All spans created in this scope will have access to baggage
    try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
        // Baggage is automatically propagated
    }
}
```

## Resource Attributes

The SDK automatically sets these resource attributes:
- `service.name`: Service name (from configuration)
- `workspace.id`: CozeLoop workspace ID

You can add custom resource attributes:

```java
Resource customResource = Resource.builder()
    .put(AttributeKey.stringKey("deployment.environment"), "production")
    .put(AttributeKey.stringKey("service.version"), "1.0.0")
    .build();
```

## Best Practices

### 1. Use Try-With-Resources

Always use try-with-resources to ensure spans are properly closed:

```java
try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
    // Your code
}
```

### 2. Set Attributes Early

Set important attributes as early as possible:

```java
try (CozeLoopSpan span = client.startSpan("llm-call", "llm")) {
    span.setModelProvider("openai");
    span.setModel("gpt-4");
    // Then make the actual call
}
```

### 3. Handle Errors Properly

Always set error status when exceptions occur:

```java
try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
    // Your code
} catch (Exception e) {
    span.setError(e);
    span.setStatusCode(1);
    throw e;
}
```

### 4. Use Appropriate Span Types

Use semantic span types:
- `"llm"`: For LLM API calls
- `"tool"`: For tool/function calls
- `"custom"`: For custom operations

### 5. Batch Configuration

Tune batch settings based on your workload:

```java
TraceConfig config = TraceConfig.builder()
    .maxQueueSize(4096)        // Larger queue for high throughput
    .batchSize(1024)           // Larger batches
    .scheduleDelayMillis(1000)  // More frequent exports
    .exportTimeoutMillis(60000) // Longer timeout
    .build();
```

## Integration with Other OpenTelemetry Instrumentation

The SDK can work alongside other OpenTelemetry instrumentation:

```java
// Your application might have other OpenTelemetry instrumentation
// CozeLoop SDK will use the same TracerProvider if already initialized
// Otherwise, it will create and register its own

// Both will work together seamlessly
```

## Troubleshooting

### Spans Not Appearing

1. **Check client is not closed**: Ensure `client.close()` is called only at shutdown
2. **Check batch delay**: Spans may be queued, wait for batch export
3. **Check logs**: Look for export errors in logs
4. **Force flush**: Call `tracerProvider.shutdown()` to flush pending spans

### Performance Issues

1. **Reduce batch size**: Smaller batches = more frequent exports
2. **Increase queue size**: Prevents span drops under load
3. **Adjust schedule delay**: Balance between latency and throughput

### Context Not Propagating

1. **Ensure span is current**: Use try-with-resources or `span.makeCurrent()`
2. **Check thread boundaries**: Context propagates automatically within threads
3. **For async**: Use OpenTelemetry's context propagation utilities

## References

- [OpenTelemetry Java Documentation](https://opentelemetry.io/docs/instrumentation/java/)
- [OpenTelemetry Specification](https://opentelemetry.io/docs/specs/otel/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)

