package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Tool definition. */
public class Tool {
  @JsonProperty("type")
  private String type;

  @JsonProperty("function")
  private Function function;

  public Tool() {}

  // Getters and Setters
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Function getFunction() {
    return function;
  }

  public void setFunction(Function function) {
    this.function = function;
  }

  public Tool deepCopy() {
    Tool copy = new Tool();
    copy.type = this.type;
    if (this.function != null) {
      copy.function = this.function.deepCopy();
    }
    return copy;
  }

  public static class Function {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parameters")
    private String parameters;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getParameters() {
      return parameters;
    }

    public void setParameters(String parameters) {
      this.parameters = parameters;
    }

    public Function deepCopy() {
      Function copy = new Function();
      copy.name = this.name;
      copy.description = this.description;
      copy.parameters = this.parameters;
      return copy;
    }
  }
}
