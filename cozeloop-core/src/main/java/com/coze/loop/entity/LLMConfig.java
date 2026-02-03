package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** LLM configuration. */
public class LLMConfig {
  @JsonProperty("temperature")
  private Double temperature;

  @JsonProperty("max_tokens")
  private Integer maxTokens;

  @JsonProperty("top_k")
  private Integer topK;

  @JsonProperty("top_p")
  private Double topP;

  @JsonProperty("frequency_penalty")
  private Double frequencyPenalty;

  @JsonProperty("presence_penalty")
  private Double presencePenalty;

  @JsonProperty("json_mode")
  private Boolean jsonMode;

  public LLMConfig() {}

  // Getters and Setters
  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public Integer getTopK() {
    return topK;
  }

  public void setTopK(Integer topK) {
    this.topK = topK;
  }

  public Double getTopP() {
    return topP;
  }

  public void setTopP(Double topP) {
    this.topP = topP;
  }

  public Double getFrequencyPenalty() {
    return frequencyPenalty;
  }

  public void setFrequencyPenalty(Double frequencyPenalty) {
    this.frequencyPenalty = frequencyPenalty;
  }

  public Double getPresencePenalty() {
    return presencePenalty;
  }

  public void setPresencePenalty(Double presencePenalty) {
    this.presencePenalty = presencePenalty;
  }

  public Boolean getJsonMode() {
    return jsonMode;
  }

  public void setJsonMode(Boolean jsonMode) {
    this.jsonMode = jsonMode;
  }

  public LLMConfig deepCopy() {
    LLMConfig copy = new LLMConfig();
    copy.temperature = this.temperature;
    copy.maxTokens = this.maxTokens;
    copy.topK = this.topK;
    copy.topP = this.topP;
    copy.frequencyPenalty = this.frequencyPenalty;
    copy.presencePenalty = this.presencePenalty;
    copy.jsonMode = this.jsonMode;
    return copy;
  }
}
