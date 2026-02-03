package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelChoice {
  @JsonProperty("finish_reason")
  private String finishReason;

  @JsonProperty("index")
  private Long index;

  @JsonProperty("message")
  private ModelMessage message;

  public ModelChoice(ModelMessage assistant, String finishReason, Long index) {
    this.message = assistant;
    this.finishReason = finishReason;
    this.index = index;
  }

  // Getters and Setters
  public String getFinishReason() {
    return finishReason;
  }

  public void setFinishReason(String finishReason) {
    this.finishReason = finishReason;
  }

  public Long getIndex() {
    return index;
  }

  public void setIndex(Long index) {
    this.index = index;
  }

  public ModelMessage getMessage() {
    return message;
  }

  public void setMessage(ModelMessage message) {
    this.message = message;
  }
}
