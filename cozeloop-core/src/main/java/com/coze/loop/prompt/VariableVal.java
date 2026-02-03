package com.coze.loop.prompt;

import java.util.List;

import com.coze.loop.entity.ContentPart;
import com.coze.loop.entity.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Variable value for Execute request. Supports different types: string value, placeholder messages,
 * or multipart values.
 */
public class VariableVal {
  @JsonProperty("key")
  private String key;

  @JsonProperty("value")
  private String value;

  @JsonProperty("placeholder_messages")
  private List<Message> placeholderMessages;

  @JsonProperty("multi_part_values")
  private List<ContentPart> multiPartValues;

  public VariableVal() {}

  public VariableVal(String key) {
    this.key = key;
  }

  // Getters and Setters
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Message> getPlaceholderMessages() {
    return placeholderMessages;
  }

  public void setPlaceholderMessages(List<Message> placeholderMessages) {
    this.placeholderMessages = placeholderMessages;
  }

  public List<ContentPart> getMultiPartValues() {
    return multiPartValues;
  }

  public void setMultiPartValues(List<ContentPart> multiPartValues) {
    this.multiPartValues = multiPartValues;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final VariableVal variableVal = new VariableVal();

    public Builder key(String key) {
      variableVal.key = key;
      return this;
    }

    public Builder value(String value) {
      variableVal.value = value;
      return this;
    }

    public Builder placeholderMessages(List<Message> placeholderMessages) {
      variableVal.placeholderMessages = placeholderMessages;
      return this;
    }

    public Builder multiPartValues(List<ContentPart> multiPartValues) {
      variableVal.multiPartValues = multiPartValues;
      return this;
    }

    public VariableVal build() {
      return variableVal;
    }
  }
}
