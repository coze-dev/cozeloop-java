// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.exception;

/**
 * Exception for export/upload operations.
 */
public class ExportException extends CozeLoopException {
    private static final long serialVersionUID = 1L;
    
    public ExportException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ExportException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ExportException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public ExportException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

