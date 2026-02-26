package com.coze.loop.trace;

public enum SpanFinishEvent {
  SPAN_QUEUE_ENTRY_RATE("queue_manager.span_entry.rate"),

  FLUSH_SPAN_RATE("exporter.span_flush.rate");

  private final String value;

  SpanFinishEvent(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
