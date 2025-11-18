package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Message entity.
 */
public class Message {
    @JsonProperty("role")
    private Role role;
    
    @JsonProperty("reasoning_content")
    private String reasoningContent;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("parts")
    private List<ContentPart> parts;
    
    @JsonProperty("tool_call_id")
    private String toolCallId;
    
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
    
    public Message() {
    }
    
    public Message(Role role) {
        this.role = role;
    }
    
    // Getters and Setters
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getReasoningContent() {
        return reasoningContent;
    }
    
    public void setReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<ContentPart> getParts() {
        return parts;
    }
    
    public void setParts(List<ContentPart> parts) {
        this.parts = parts;
    }
    
    public String getToolCallId() {
        return toolCallId;
    }
    
    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
    
    /**
     * Deep copy this message.
     *
     * @return a new Message instance with copied values
     */
    public Message deepCopy() {
        Message copy = new Message();
        copy.role = this.role;
        copy.reasoningContent = this.reasoningContent;
        copy.content = this.content;
        copy.toolCallId = this.toolCallId;
        
        if (this.parts != null) {
            copy.parts = new ArrayList<>();
            for (ContentPart part : this.parts) {
                copy.parts.add(part.deepCopy());
            }
        }
        
        // Tool calls - shallow copy for now
        if (this.toolCalls != null) {
            copy.toolCalls = new ArrayList<>(this.toolCalls);
        }
        
        return copy;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final Message message = new Message();
        
        public Builder role(Role role) {
            message.role = role;
            return this;
        }
        
        public Builder content(String content) {
            message.content = content;
            return this;
        }
        
        public Builder parts(List<ContentPart> parts) {
            message.parts = parts;
            return this;
        }
        
        public Builder addPart(ContentPart part) {
            if (message.parts == null) {
                message.parts = new ArrayList<>();
            }
            message.parts.add(part);
            return this;
        }
        
        public Message build() {
            return message;
        }
    }
}

