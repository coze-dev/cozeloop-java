// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token usage information.
 */
public class TokenUsage {
    @JsonProperty("input_tokens")
    private int inputTokens;
    
    @JsonProperty("output_tokens")
    private int outputTokens;
    
    public TokenUsage() {
    }
    
    public TokenUsage(int inputTokens, int outputTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }
    
    // Getters and Setters
    public int getInputTokens() {
        return inputTokens;
    }
    
    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }
    
    public int getOutputTokens() {
        return outputTokens;
    }
    
    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final TokenUsage usage = new TokenUsage();
        
        public Builder inputTokens(int inputTokens) {
            usage.inputTokens = inputTokens;
            return this;
        }
        
        public Builder outputTokens(int outputTokens) {
            usage.outputTokens = outputTokens;
            return this;
        }
        
        public TokenUsage build() {
            return usage;
        }
    }
}

