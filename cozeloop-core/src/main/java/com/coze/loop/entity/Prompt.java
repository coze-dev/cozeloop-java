package com.coze.loop.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Prompt entity. */
public class Prompt {
  @JsonProperty("workspace_id")
  private String workspaceId;

  @JsonProperty("prompt_key")
  private String promptKey;

  @JsonProperty("version")
  private String version;

  @JsonProperty("prompt_template")
  private PromptTemplate promptTemplate;

  @JsonProperty("tools")
  private List<Tool> tools;

  @JsonProperty("tool_call_config")
  private ToolCallConfig toolCallConfig;

  @JsonProperty("llm_config")
  private LLMConfig llmConfig;

  public Prompt() {}

  // Getters and Setters
  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public String getPromptKey() {
    return promptKey;
  }

  public void setPromptKey(String promptKey) {
    this.promptKey = promptKey;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public PromptTemplate getPromptTemplate() {
    return promptTemplate;
  }

  public void setPromptTemplate(PromptTemplate promptTemplate) {
    this.promptTemplate = promptTemplate;
  }

  public List<Tool> getTools() {
    return tools;
  }

  public void setTools(List<Tool> tools) {
    this.tools = tools;
  }

  public ToolCallConfig getToolCallConfig() {
    return toolCallConfig;
  }

  public void setToolCallConfig(ToolCallConfig toolCallConfig) {
    this.toolCallConfig = toolCallConfig;
  }

  public LLMConfig getLlmConfig() {
    return llmConfig;
  }

  public void setLlmConfig(LLMConfig llmConfig) {
    this.llmConfig = llmConfig;
  }

  /**
   * Deep copy this prompt.
   *
   * @return a new Prompt instance with copied values
   */
  public Prompt deepCopy() {
    Prompt copy = new Prompt();
    copy.workspaceId = this.workspaceId;
    copy.promptKey = this.promptKey;
    copy.version = this.version;

    if (this.promptTemplate != null) {
      copy.promptTemplate = this.promptTemplate.deepCopy();
    }

    if (this.tools != null) {
      copy.tools = new ArrayList<>();
      for (Tool tool : this.tools) {
        copy.tools.add(tool.deepCopy());
      }
    }

    if (this.toolCallConfig != null) {
      copy.toolCallConfig = this.toolCallConfig.deepCopy();
    }

    if (this.llmConfig != null) {
      copy.llmConfig = this.llmConfig.deepCopy();
    }

    return copy;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Prompt prompt = new Prompt();

    public Builder workspaceId(String workspaceId) {
      prompt.workspaceId = workspaceId;
      return this;
    }

    public Builder promptKey(String promptKey) {
      prompt.promptKey = promptKey;
      return this;
    }

    public Builder version(String version) {
      prompt.version = version;
      return this;
    }

    public Builder promptTemplate(PromptTemplate promptTemplate) {
      prompt.promptTemplate = promptTemplate;
      return this;
    }

    public Builder tools(List<Tool> tools) {
      prompt.tools = tools;
      return this;
    }

    public Builder llmConfig(LLMConfig llmConfig) {
      prompt.llmConfig = llmConfig;
      return this;
    }

    public Prompt build() {
      return prompt;
    }
  }
}
