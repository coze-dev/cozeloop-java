package com.coze.loop.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Interceptor to retry failed requests with exponential backoff. */
public class RetryInterceptor implements Interceptor {
  private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
  private static final int INITIAL_BACKOFF_MS = 100;
  private static final int MAX_BACKOFF_MS = 10000;

  private final int maxRetries;

  public RetryInterceptor(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = null;
    IOException lastException = null;

    for (int attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        if (attempt > 0) {
          long backoffMs = calculateBackoff(attempt);
          logger.debug(
              "Retrying request (attempt {}/{}), backoff: {}ms", attempt, maxRetries, backoffMs);

          try {
            TimeUnit.MILLISECONDS.sleep(backoffMs);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry interrupted", e);
          }
        }

        response = chain.proceed(request);

        // Check if response is successful or not retryable
        if (response.isSuccessful() || !isRetryableStatusCode(response.code())) {
          return response;
        }

        // Close the unsuccessful response body
        if (response.body() != null) {
          response.body().close();
        }

      } catch (IOException e) {
        lastException = e;
        logger.warn(
            "Request failed (attempt {}/{}): {}", attempt + 1, maxRetries + 1, e.getMessage());

        // If this is the last attempt, throw the exception
        if (attempt == maxRetries) {
          throw e;
        }
      }
    }

    // If we've exhausted retries and have a response, return it
    if (response != null) {
      return response;
    }

    // Otherwise throw the last exception
    throw lastException != null
        ? lastException
        : new IOException("Request failed after " + maxRetries + " retries");
  }

  /** Calculate exponential backoff time in milliseconds. */
  private long calculateBackoff(int attempt) {
    long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
    return Math.min(backoff, MAX_BACKOFF_MS);
  }

  /** Check if the status code is retryable. */
  private boolean isRetryableStatusCode(int statusCode) {
    return statusCode == 429
        || // Too Many Requests
        statusCode >= 500; // Server errors
  }
}
