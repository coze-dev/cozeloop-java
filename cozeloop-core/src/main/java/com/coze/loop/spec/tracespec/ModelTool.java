package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelTool {
    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private ModelToolFunction function;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ModelToolFunction getFunction() {
        return function;
    }

    public void setFunction(ModelToolFunction function) {
        this.function = function;
    }
}
