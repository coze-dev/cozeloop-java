package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tool call configuration.
 */
public class ToolCallConfig {
    @JsonProperty("tool_choice")
    private String toolChoice;
    
    public ToolCallConfig() {
    }
    
    // Getters and Setters
    public String getToolChoice() {
        return toolChoice;
    }
    
    public void setToolChoice(String toolChoice) {
        this.toolChoice = toolChoice;
    }
    
    public ToolCallConfig deepCopy() {
        ToolCallConfig copy = new ToolCallConfig();
        copy.toolChoice = this.toolChoice;
        return copy;
    }
}

