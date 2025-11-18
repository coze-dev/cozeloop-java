// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Content part type enum.
 */
public enum ContentType {
    TEXT("text"),
    IMAGE_URL("image_url"),
    BASE64_DATA("base64_data"),
    MULTI_PART_VARIABLE("multi_part_variable");
    
    private final String value;
    
    ContentType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public static ContentType fromValue(String value) {
        for (ContentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid content type: " + value);
    }
}

