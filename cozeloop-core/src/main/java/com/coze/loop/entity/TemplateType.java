package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Template type enum.
 */
public enum TemplateType {
    NORMAL("normal"),
    JINJA2("jinja2");
    
    private final String value;
    
    TemplateType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static TemplateType fromValue(String value) {
        for (TemplateType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid template type: " + value);
    }
}

