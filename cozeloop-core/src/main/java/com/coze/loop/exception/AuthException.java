package com.coze.loop.exception;

/**
 * Exception for authentication-related operations.
 */
public class AuthException extends CozeLoopException {
    private static final long serialVersionUID = 1L;
    
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public AuthException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

