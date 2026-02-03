package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromptArgument {
  @JsonProperty("key")
  private String key;

  @JsonProperty("value")
  private Object value;

  @JsonProperty("source")
  private String source;

  @JsonProperty("value_type")
  private String valueType;

  // Getters and Setters
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
}
