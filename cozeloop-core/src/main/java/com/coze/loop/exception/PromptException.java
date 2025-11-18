// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.exception;

/**
 * Exception for prompt-related operations.
 */
public class PromptException extends CozeLoopException {
    private static final long serialVersionUID = 1L;
    
    public PromptException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public PromptException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public PromptException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public PromptException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

