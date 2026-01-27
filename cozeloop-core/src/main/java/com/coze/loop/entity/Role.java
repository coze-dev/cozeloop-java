package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message role enum.
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool"),
    PLACEHOLDER("placeholder");
    
    private final String value;
    
    Role(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static Role fromValue(String value) {
        for (Role role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}

