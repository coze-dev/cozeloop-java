package com.coze.loop.spec.tracespec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** PromptInput is the input of prompt span, for tag key: input */
public class PromptInput {
  @JsonProperty("templates")
  private List<ModelMessage> templates;

  @JsonProperty("arguments")
  private List<PromptArgument> arguments;

  // Getters and Setters
  public List<ModelMessage> getTemplates() {
    return templates;
  }

  public void setTemplates(List<ModelMessage> templates) {
    this.templates = templates;
  }

  public List<PromptArgument> getArguments() {
    return arguments;
  }

  public void setArguments(List<PromptArgument> arguments) {
    this.arguments = arguments;
  }
}
