package com.coze.loop.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coze.loop.auth.Auth;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.internal.JsonUtils;

import io.opentelemetry.context.propagation.ContextPropagators;
import okhttp3.*;

/** HTTP client based on OkHttp. */
public class HttpClient {
  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
  private static final MediaType JSON_MEDIA_TYPE =
      MediaType.parse("application/json; charset=utf-8");

  private final OkHttpClient okHttpClient;
  private final Auth auth;
  private final ContextPropagators propagators;

  /**
   * Create an HttpClient with default configuration.
   *
   * @param auth the authentication provider
   */
  public HttpClient(Auth auth) {
    this(auth, new HttpConfig(), null);
  }

  /**
   * Create an HttpClient with custom configuration.
   *
   * @param auth the authentication provider
   * @param config the HTTP configuration
   */
  public HttpClient(Auth auth, HttpConfig config) {
    this(auth, config, null);
  }

  /**
   * Create an HttpClient with custom configuration and trace propagators.
   *
   * @param auth the authentication provider
   * @param config the HTTP configuration
   * @param propagators the trace context propagators
   */
  public HttpClient(Auth auth, HttpConfig config, ContextPropagators propagators) {
    this.auth = auth;
    this.propagators = propagators;
    this.okHttpClient = buildOkHttpClient(config);
  }

  /** Build OkHttp client with interceptors. */
  private OkHttpClient buildOkHttpClient(HttpConfig config) {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
            .writeTimeout(config.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
            .connectionPool(
                new ConnectionPool(
                    config.getMaxIdleConnections(),
                    config.getKeepAliveDurationMinutes(),
                    TimeUnit.MINUTES))
            .retryOnConnectionFailure(true);

    // Add interceptors
    if (auth != null) {
      builder.addInterceptor(new AuthInterceptor(auth, propagators));
    }
    builder.addInterceptor(new RetryInterceptor(config.getMaxRetries()));

    if (logger.isDebugEnabled()) {
      builder.addInterceptor(new LoggingInterceptor());
    }

    return builder.build();
  }

  /**
   * Execute a GET request.
   *
   * @param url the URL
   * @return response body as string
   */
  public String get(String url) {
    Request request = new Request.Builder().url(url).get().build();

    return execute(request);
  }

  /**
   * Execute a POST request with JSON body.
   *
   * @param url the URL
   * @param body the request body object
   * @return response body as string
   */
  public String post(String url, Object body) {
    return post(url, body, null);
  }

  /**
   * Execute a POST request with JSON body and custom headers.
   *
   * @param url the URL
   * @param body the request body object
   * @param headers custom headers
   * @return response body as string
   */
  public String post(String url, Object body, java.util.Map<String, String> headers) {
    String json = JsonUtils.toJson(body);
    RequestBody requestBody = RequestBody.create(json, JSON_MEDIA_TYPE);

    Request.Builder builder = new Request.Builder().url(url).post(requestBody);

    if (headers != null) {
      headers.forEach(builder::header);
    }

    return execute(builder.build());
  }

  /**
   * Execute a POST request with multipart form data.
   *
   * @param url the URL
   * @param formData the multipart form data
   * @return response body as string
   */
  public String postMultipart(String url, MultipartBody formData) {
    Request request = new Request.Builder().url(url).post(formData).build();

    return execute(request);
  }

  /**
   * Execute a POST request with JSON body and return Response for streaming. The caller is
   * responsible for closing the Response.
   *
   * @param url the URL
   * @param body the request body object
   * @return Response object for streaming
   * @throws IOException if the request fails
   */
  public Response postStream(String url, Object body) throws IOException {
    String json = JsonUtils.toJson(body);
    RequestBody requestBody = RequestBody.create(json, JSON_MEDIA_TYPE);

    Request request = new Request.Builder().url(url).post(requestBody).build();

    Response response = okHttpClient.newCall(request).execute();

    if (!response.isSuccessful()) {
      String errorBody = response.body() != null ? response.body().string() : "";
      response.close();
      throw new CozeLoopException(
          ErrorCode.NETWORK_ERROR,
          String.format("HTTP request failed with code: %d, body: %s", response.code(), errorBody));
    }

    return response;
  }

  /** Execute the request. */
  private String execute(Request request) {
    try (Response response = okHttpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        String errorBody = response.body() != null ? response.body().string() : "";
        throw new CozeLoopException(
            ErrorCode.NETWORK_ERROR,
            String.format(
                "HTTP request failed with code: %d, body: %s", response.code(), errorBody));
      }

      // Log Content-Type for debugging
      String contentType = response.header("Content-Type");
      if (logger.isDebugEnabled() && contentType != null) {
        logger.debug("Response Content-Type: {}", contentType);
      }

      ResponseBody body = response.body();
      return body != null ? body.string() : "";
    } catch (IOException e) {
      throw new CozeLoopException(ErrorCode.NETWORK_ERROR, "HTTP request failed", e);
    }
  }

  /** Close the HTTP client and release resources. */
  public void close() {
    if (okHttpClient != null) {
      okHttpClient.dispatcher().executorService().shutdown();
      okHttpClient.connectionPool().evictAll();
    }
  }
}
