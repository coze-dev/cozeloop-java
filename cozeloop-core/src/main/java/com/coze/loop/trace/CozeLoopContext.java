package com.coze.loop.trace;

import org.jetbrains.annotations.Nullable;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;

/**
 * Wrapper for OpenTelemetry Context to shield internal implementation.
 *
 * <p>This class provides a way to work with trace context without directly exposing OpenTelemetry
 * APIs.
 */
public class CozeLoopContext implements Context {
  private final Context context;

  /**
   * Create a new CozeLoopContext wrapping an OpenTelemetry Context.
   *
   * @param context the underlying OpenTelemetry context
   */
  public CozeLoopContext(Context context) {
    this.context = context;
  }

  /**
   * Get the current context from thread-local storage.
   *
   * @return the current context
   */
  public static CozeLoopContext current() {
    return new CozeLoopContext(Context.current());
  }

  @Nullable
  @Override
  public <V> V get(ContextKey<V> contextKey) {
    return context.get(contextKey);
  }

  @Override
  public <V> Context with(ContextKey<V> contextKey, V v) {
    return context.with(contextKey, v);
  }
}
