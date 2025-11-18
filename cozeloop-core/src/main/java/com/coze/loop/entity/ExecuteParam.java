package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Parameters for executing a prompt.
 */
public class ExecuteParam {
    @JsonProperty("prompt_key")
    private String promptKey;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("variable_vals")
    private Map<String, Object> variableVals;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    public ExecuteParam() {
    }
    
    // Getters and Setters
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
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Map<String, Object> getVariableVals() {
        return variableVals;
    }
    
    public void setVariableVals(Map<String, Object> variableVals) {
        this.variableVals = variableVals;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ExecuteParam param = new ExecuteParam();
        
        public Builder promptKey(String promptKey) {
            param.promptKey = promptKey;
            return this;
        }
        
        public Builder version(String version) {
            param.version = version;
            return this;
        }
        
        public Builder label(String label) {
            param.label = label;
            return this;
        }
        
        public Builder variableVals(Map<String, Object> variableVals) {
            param.variableVals = variableVals;
            return this;
        }
        
        public Builder messages(List<Message> messages) {
            param.messages = messages;
            return this;
        }
        
        public ExecuteParam build() {
            return param;
        }
    }
}

