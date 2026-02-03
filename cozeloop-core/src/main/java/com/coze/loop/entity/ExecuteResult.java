package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Result of executing a prompt. */
public class ExecuteResult {
  @JsonProperty("message")
  private Message message;

  @JsonProperty("finish_reason")
  private String finishReason;

  @JsonProperty("usage")
  private TokenUsage usage;

  public ExecuteResult() {}

  // Getters and Setters
  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public String getFinishReason() {
    return finishReason;
  }

  public void setFinishReason(String finishReason) {
    this.finishReason = finishReason;
  }

  public TokenUsage getUsage() {
    return usage;
  }

  public void setUsage(TokenUsage usage) {
    this.usage = usage;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ExecuteResult result = new ExecuteResult();

    public Builder message(Message message) {
      result.message = message;
      return this;
    }

    public Builder finishReason(String finishReason) {
      result.finishReason = finishReason;
      return this;
    }

    public Builder usage(TokenUsage usage) {
      result.usage = usage;
      return this;
    }

    public ExecuteResult build() {
      return result;
    }
  }
}
