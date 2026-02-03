package com.coze.loop.spec.tracespec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** PromptOutput is the output of prompt span, for tag key: output */
public class PromptOutput {
  @JsonProperty("prompts")
  private List<ModelMessage> prompts;

  // Getters and Setters
  public List<ModelMessage> getPrompts() {
    return prompts;
  }

  public void setPrompts(List<ModelMessage> prompts) {
    this.prompts = prompts;
  }
}
