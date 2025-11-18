// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.exception;

/**
 * Error codes for CozeLoop SDK exceptions.
 */
public enum ErrorCode {
    /**
     * Invalid parameter provided
     */
    INVALID_PARAM(1001, "Invalid parameter"),
    
    /**
     * Authentication failed
     */
    AUTH_FAILED(1002, "Authentication failed"),
    
    /**
     * Network error occurred
     */
    NETWORK_ERROR(1003, "Network error"),
    
    /**
     * Export/upload failed
     */
    EXPORT_FAILED(1004, "Export failed"),
    
    /**
     * Template rendering error
     */
    TEMPLATE_RENDER_ERROR(1005, "Template render error"),
    
    /**
     * Client has been closed
     */
    CLIENT_CLOSED(1006, "Client has been closed"),
    
    /**
     * Prompt not found
     */
    PROMPT_NOT_FOUND(1007, "Prompt not found"),
    
    /**
     * Internal error
     */
    INTERNAL_ERROR(1099, "Internal error");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

