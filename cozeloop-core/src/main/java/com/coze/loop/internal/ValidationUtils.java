package com.coze.loop.internal;

import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;

/**
 * Validation utility class.
 */
public final class ValidationUtils {
    
    private ValidationUtils() {
        // Utility class
    }
    
    /**
     * Check if a string is not null and not empty.
     *
     * @param value the string to check
     * @param paramName the parameter name for error message
     * @throws CozeLoopException if validation fails
     */
    public static void requireNonEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CozeLoopException(ErrorCode.INVALID_PARAM, 
                paramName + " must not be null or empty");
        }
    }
    
    /**
     * Check if an object is not null.
     *
     * @param value the object to check
     * @param paramName the parameter name for error message
     * @throws CozeLoopException if validation fails
     */
    public static void requireNonNull(Object value, String paramName) {
        if (value == null) {
            throw new CozeLoopException(ErrorCode.INVALID_PARAM, 
                paramName + " must not be null");
        }
    }
    
    /**
     * Check if a number is positive.
     *
     * @param value the number to check
     * @param paramName the parameter name for error message
     * @throws CozeLoopException if validation fails
     */
    public static void requirePositive(long value, String paramName) {
        if (value <= 0) {
            throw new CozeLoopException(ErrorCode.INVALID_PARAM, 
                paramName + " must be positive");
        }
    }
    
    /**
     * Check if a number is non-negative.
     *
     * @param value the number to check
     * @param paramName the parameter name for error message
     * @throws CozeLoopException if validation fails
     */
    public static void requireNonNegative(long value, String paramName) {
        if (value < 0) {
            throw new CozeLoopException(ErrorCode.INVALID_PARAM, 
                paramName + " must be non-negative");
        }
    }
    
    /**
     * Check if a condition is true.
     *
     * @param condition the condition to check
     * @param message the error message
     * @throws CozeLoopException if condition is false
     */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new CozeLoopException(ErrorCode.INVALID_PARAM, message);
        }
    }
}

