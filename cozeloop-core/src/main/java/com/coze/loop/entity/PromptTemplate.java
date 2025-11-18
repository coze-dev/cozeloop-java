// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt template entity.
 */
public class PromptTemplate {
    @JsonProperty("template_type")
    private TemplateType templateType;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("variable_defs")
    private List<VariableDef> variableDefs;
    
    public PromptTemplate() {
    }
    
    // Getters and Setters
    public TemplateType getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public List<VariableDef> getVariableDefs() {
        return variableDefs;
    }
    
    public void setVariableDefs(List<VariableDef> variableDefs) {
        this.variableDefs = variableDefs;
    }
    
    /**
     * Deep copy this prompt template.
     *
     * @return a new PromptTemplate instance with copied values
     */
    public PromptTemplate deepCopy() {
        PromptTemplate copy = new PromptTemplate();
        copy.templateType = this.templateType;
        
        if (this.messages != null) {
            copy.messages = new ArrayList<>();
            for (Message message : this.messages) {
                copy.messages.add(message.deepCopy());
            }
        }
        
        if (this.variableDefs != null) {
            copy.variableDefs = new ArrayList<>();
            for (VariableDef def : this.variableDefs) {
                copy.variableDefs.add(def.deepCopy());
            }
        }
        
        return copy;
    }
}

