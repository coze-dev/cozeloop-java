package com.coze.loop.spring.test;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

import java.lang.reflect.Field;

/**
 * Utility class for OpenTelemetry testing.
 */
public class OpenTelemetryTestUtils {

    /**
     * Reset the GlobalOpenTelemetry instance.
     * This is useful for tests that need to create multiple OpenTelemetry instances.
     */
    public static void resetGlobalOpenTelemetry() {
        try {
            // Try to reset using reflection
            Field globalField = GlobalOpenTelemetry.class.getDeclaredField("globalOpenTelemetry");
            globalField.setAccessible(true);
            globalField.set(null, null);
            
            Field initializedField = GlobalOpenTelemetry.class.getDeclaredField("initialized");
            initializedField.setAccessible(true);
            initializedField.setBoolean(null, false);
        } catch (Exception e) {
            // Reflection failed - cannot reset
            // This is expected in some scenarios where the internal structure has changed
            // The CozeLoopTracerProvider will handle this by checking if GlobalOpenTelemetry
            // is already set before trying to register globally
        }
    }
}

