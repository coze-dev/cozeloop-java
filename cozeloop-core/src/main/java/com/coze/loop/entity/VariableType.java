// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Variable type enum.
 */
public enum VariableType {
    STRING("string"),
    PLACEHOLDER("placeholder"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    FLOAT("float"),
    OBJECT("object"),
    ARRAY_STRING("array<string>"),
    ARRAY_BOOLEAN("array<boolean>"),
    ARRAY_INTEGER("array<integer>"),
    ARRAY_FLOAT("array<float>"),
    ARRAY_OBJECT("array<object>"),
    MULTI_PART("multi_part");
    
    private final String value;
    
    VariableType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static VariableType fromValue(String value) {
        for (VariableType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid variable type: " + value);
    }
}

