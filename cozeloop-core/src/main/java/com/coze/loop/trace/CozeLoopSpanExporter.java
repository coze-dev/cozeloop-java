package com.coze.loop.trace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.coze.loop.internal.CozeLoopLogger;

import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CozeLoopSpanExporter implements SpanExporter {
  private static final Logger logger = CozeLoopLogger.getLogger(CozeLoopSpanExporter.class);
  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");
  private static final String LOG_ID_HEADER = "X-TT-LogID";

  private final String endpoint;
  private final Supplier<Map<String, String>> headersSupplier;
  private final OkHttpClient httpClient;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final FinishEventProcessor finishEventProcessor;

  private CozeLoopSpanExporter(Builder builder) {
    this.endpoint = builder.endpoint;
    this.headersSupplier = builder.headersSupplier;
    this.httpClient = buildHttpClient(builder);
    this.finishEventProcessor = builder.finishEventProcessor;
  }

  private OkHttpClient buildHttpClient(Builder builder) {
    return new OkHttpClient.Builder()
        .connectTimeout(builder.connectTimeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(builder.readTimeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(builder.writeTimeoutMillis, TimeUnit.MILLISECONDS)
        .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
        .retryOnConnectionFailure(true)
        .build();
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    if (spans.isEmpty()) {
      return CompletableResultCode.ofSuccess();
    }

    CompletableResultCode resultCode = new CompletableResultCode();

    try {
      TraceRequestMarshaler marshaler = TraceRequestMarshaler.create(spans);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      marshaler.writeBinaryTo(baos);
      byte[] body = baos.toByteArray();

      Request.Builder requestBuilder =
          new Request.Builder()
              .url(endpoint)
              .post(RequestBody.Companion.create(body, PROTOBUF_MEDIA_TYPE));

      Map<String, String> headers = headersSupplier.get();
      if (headers != null) {
        headers.forEach(requestBuilder::header);
      }

      Request request = requestBuilder.build();

      try (Response response = httpClient.newCall(request).execute()) {
        String logId = response.header(LOG_ID_HEADER);
        int statusCode = response.code();

        if (response.isSuccessful()) {
          if (logId != null) {
            logger.debug("Trace export successful, spans: {}, logId: {}", spans.size(), logId);
          } else {
            logger.debug("Trace export successful, spans: {}", spans.size());
          }
          notifyFinishEvent(spans.size(), false, null);
          resultCode.succeed();
        } else {
          String responseBody = response.body() != null ? response.body().string() : "";
          logger.warn(
              "Trace export failed, status: {}, logId: {}, body: {}",
              statusCode,
              logId,
              responseBody);
          notifyFinishEvent(spans.size(), true, "status: " + statusCode);
          resultCode.fail();
        }
      }
    } catch (IOException e) {
      logger.error("Trace export error", e);
      notifyFinishEvent(spans.size(), true, e.getMessage());
      resultCode.fail();
    }

    return resultCode;
  }

  private void notifyFinishEvent(int spanCount, boolean isFail, String detailMsg) {
    if (finishEventProcessor != null) {
      try {
        FinishEventInfo info =
            FinishEventInfo.builder()
                .eventType(SpanFinishEvent.FLUSH_SPAN_RATE)
                .isEventFail(isFail)
                .itemNum(spanCount)
                .detailMsg(detailMsg)
                .build();
        finishEventProcessor.process(info);
      } catch (Exception e) {
        logger.warn("FinishEventProcessor error", e);
      }
    }
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      return CompletableResultCode.ofSuccess();
    }

    httpClient.dispatcher().executorService().shutdown();
    httpClient.connectionPool().evictAll();
    logger.info("CozeLoopSpanExporter shutdown");
    return CompletableResultCode.ofSuccess();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String endpoint;
    private Supplier<Map<String, String>> headersSupplier = java.util.Collections::emptyMap;
    private long connectTimeoutMillis = 10000;
    private long readTimeoutMillis = 10000;
    private long writeTimeoutMillis = 10000;
    private FinishEventProcessor finishEventProcessor;

    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder setHeaders(Supplier<Map<String, String>> headersSupplier) {
      this.headersSupplier = headersSupplier;
      return this;
    }

    public Builder setConnectTimeout(long timeout, TimeUnit unit) {
      this.connectTimeoutMillis = unit.toMillis(timeout);
      return this;
    }

    public Builder setReadTimeout(long timeout, TimeUnit unit) {
      this.readTimeoutMillis = unit.toMillis(timeout);
      return this;
    }

    public Builder setWriteTimeout(long timeout, TimeUnit unit) {
      this.writeTimeoutMillis = unit.toMillis(timeout);
      return this;
    }

    public Builder setFinishEventProcessor(FinishEventProcessor processor) {
      this.finishEventProcessor = processor;
      return this;
    }

    public CozeLoopSpanExporter build() {
      if (endpoint == null || endpoint.isEmpty()) {
        throw new IllegalArgumentException("endpoint must be set");
      }
      return new CozeLoopSpanExporter(this);
    }
  }
}
