package com.coze.loop.trace;

public class FinishEventInfo {
  private SpanFinishEvent eventType;
  private boolean isEventFail;
  private int itemNum;
  private String detailMsg;
  private FinishEventInfoExtra extraParams;

  public FinishEventInfo() {}

  public FinishEventInfo(SpanFinishEvent eventType, boolean isEventFail, int itemNum) {
    this.eventType = eventType;
    this.isEventFail = isEventFail;
    this.itemNum = itemNum;
  }

  public SpanFinishEvent getEventType() {
    return eventType;
  }

  public void setEventType(SpanFinishEvent eventType) {
    this.eventType = eventType;
  }

  public boolean isEventFail() {
    return isEventFail;
  }

  public void setEventFail(boolean eventFail) {
    isEventFail = eventFail;
  }

  public int getItemNum() {
    return itemNum;
  }

  public void setItemNum(int itemNum) {
    this.itemNum = itemNum;
  }

  public String getDetailMsg() {
    return detailMsg;
  }

  public void setDetailMsg(String detailMsg) {
    this.detailMsg = detailMsg;
  }

  public FinishEventInfoExtra getExtraParams() {
    return extraParams;
  }

  public void setExtraParams(FinishEventInfoExtra extraParams) {
    this.extraParams = extraParams;
  }

  public static class FinishEventInfoExtra {
    private boolean isRootSpan;
    private long latencyMs;

    public FinishEventInfoExtra() {}

    public FinishEventInfoExtra(boolean isRootSpan, long latencyMs) {
      this.isRootSpan = isRootSpan;
      this.latencyMs = latencyMs;
    }

    public boolean isRootSpan() {
      return isRootSpan;
    }

    public void setRootSpan(boolean rootSpan) {
      isRootSpan = rootSpan;
    }

    public long getLatencyMs() {
      return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
      this.latencyMs = latencyMs;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final FinishEventInfo info = new FinishEventInfo();

    public Builder eventType(SpanFinishEvent eventType) {
      info.eventType = eventType;
      return this;
    }

    public Builder isEventFail(boolean isEventFail) {
      info.isEventFail = isEventFail;
      return this;
    }

    public Builder itemNum(int itemNum) {
      info.itemNum = itemNum;
      return this;
    }

    public Builder detailMsg(String detailMsg) {
      info.detailMsg = detailMsg;
      return this;
    }

    public Builder extraParams(FinishEventInfoExtra extraParams) {
      info.extraParams = extraParams;
      return this;
    }

    public FinishEventInfo build() {
      return info;
    }
  }
}
