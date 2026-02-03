package com.coze.loop.spec.tracespec;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelMessage {
  @JsonProperty("role")
  private String role;

  @JsonProperty("content")
  private String content;

  @JsonProperty("reasoning_content")
  private String reasoningContent;

  @JsonProperty("parts")
  private List<ModelMessagePart> parts;

  @JsonProperty("name")
  private String name;

  @JsonProperty("tool_calls")
  private List<ModelToolCall> toolCalls;

  @JsonProperty("tool_call_id")
  private String toolCallID;

  @JsonProperty("signature")
  private String signature;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  public ModelMessage(String user, String content) {
    this.role = user;
    this.content = content;
  }

  // Getters and Setters
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getReasoningContent() {
    return reasoningContent;
  }

  public void setReasoningContent(String reasoningContent) {
    this.reasoningContent = reasoningContent;
  }

  public List<ModelMessagePart> getParts() {
    return parts;
  }

  public void setParts(List<ModelMessagePart> parts) {
    this.parts = parts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ModelToolCall> getToolCalls() {
    return toolCalls;
  }

  public void setToolCalls(List<ModelToolCall> toolCalls) {
    this.toolCalls = toolCalls;
  }

  public String getToolCallID() {
    return toolCallID;
  }

  public void setToolCallID(String toolCallID) {
    this.toolCallID = toolCallID;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
