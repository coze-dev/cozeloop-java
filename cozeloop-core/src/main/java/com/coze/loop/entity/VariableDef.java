// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Variable definition in a prompt template.
 */
public class VariableDef {
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("type")
    private VariableType type;
    
    public VariableDef() {
    }
    
    public VariableDef(String key, VariableType type) {
        this.key = key;
        this.type = type;
    }
    
    // Getters and Setters
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public VariableType getType() {
        return type;
    }
    
    public void setType(VariableType type) {
        this.type = type;
    }
    
    public VariableDef deepCopy() {
        VariableDef copy = new VariableDef();
        copy.key = this.key;
        copy.desc = this.desc;
        copy.type = this.type;
        return copy;
    }
}

