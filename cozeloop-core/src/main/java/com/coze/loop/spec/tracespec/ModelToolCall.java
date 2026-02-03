package com.coze.loop.spec.tracespec;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelToolCall {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private ModelToolCallFunction function;

    @JsonProperty("signature")
    private String signature;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
