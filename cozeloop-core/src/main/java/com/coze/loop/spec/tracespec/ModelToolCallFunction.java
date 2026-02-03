package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelToolCallFunction {
  @JsonProperty("name")
  private String name;

  @JsonProperty("arguments")
  private String arguments;

  // Getters and Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }
}
