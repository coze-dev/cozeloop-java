package com.coze.loop.exception;

/**
 * Exception for trace-related operations.
 */
public class TraceException extends CozeLoopException {
    private static final long serialVersionUID = 1L;
    
    public TraceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public TraceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public TraceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public TraceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

