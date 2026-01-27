package com.coze.loop.internal;

import java.security.SecureRandom;
import java.util.Random;

/**
 * ID generator utility class.
 */
public final class IdGenerator {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final Random RANDOM = new SecureRandom();
    
    private IdGenerator() {
        // Utility class
    }
    
    /**
     * Generate a 32-character hexadecimal trace ID.
     *
     * @return trace ID
     */
    public static String generateTraceId() {
        return generateHexString(32);
    }
    
    /**
     * Generate a 16-character hexadecimal span ID.
     *
     * @return span ID
     */
    public static String generateSpanId() {
        return generateHexString(16);
    }
    
    /**
     * Generate a hexadecimal string of specified length.
     *
     * @param length the length of the string
     * @return hexadecimal string
     */
    public static String generateHexString(int length) {
        char[] buffer = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = HEX_CHARS[RANDOM.nextInt(16)];
        }
        return new String(buffer);
    }
    
    /**
     * Generate a UUID-like ID.
     *
     * @return UUID string
     */
    public static String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }
}

