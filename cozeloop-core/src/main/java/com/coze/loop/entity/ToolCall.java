package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Tool call in a message. */
public class ToolCall {
  @JsonProperty("index")
  private int index;

  @JsonProperty("id")
  private String id;

  @JsonProperty("type")
  private String type;

  @JsonProperty("function_call")
  private FunctionCall functionCall;

  public ToolCall() {}

  // Getters and Setters
  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public FunctionCall getFunctionCall() {
    return functionCall;
  }

  public void setFunctionCall(FunctionCall functionCall) {
    this.functionCall = functionCall;
  }

  public static class FunctionCall {
    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;

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
}
