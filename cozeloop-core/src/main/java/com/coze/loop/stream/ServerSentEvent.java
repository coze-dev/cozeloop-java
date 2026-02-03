package com.coze.loop.stream;

/** Represents a Server-Sent Event (SSE). */
public class ServerSentEvent {
  private String event;
  private String data;
  private String id;
  private Integer retry;

  public ServerSentEvent() {}

  public ServerSentEvent(String event, String data, String id, Integer retry) {
    this.event = event;
    this.data = data;
    this.id = id;
    this.retry = retry;
  }

  // Getters and Setters
  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getRetry() {
    return retry;
  }

  public void setRetry(Integer retry) {
    this.retry = retry;
  }

  /**
   * Check if this event has data.
   *
   * @return true if data is not null and not empty
   */
  public boolean hasData() {
    return data != null && !data.isEmpty();
  }
}
