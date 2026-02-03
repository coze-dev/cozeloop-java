package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelToolChoice {
    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private ModelToolCallFunction function;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ModelToolCallFunction getFunction() {
        return function;
    }

    public void setFunction(ModelToolCallFunction function) {
        this.function = function;
    }
}
