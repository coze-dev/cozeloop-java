package com.coze.loop.internal;

/**
 * Utility class for loading configuration from system properties and environment variables. This
 * mirrors Spring Boot's property binding behavior to provide consistent defaults across core SDK
 * and Spring Boot integration.
 */
public final class ConfigUtils {

  private ConfigUtils() {
    // Utility class
  }

  /**
   * Get a configuration value from system properties or environment variables.
   *
   * <p>For a given key like "cozeloop.workspace-id", it checks: 1. System property:
   * "cozeloop.workspace-id" 2. Environment variable: "COZELOOP_WORKSPACE_ID"
   *
   * @param key the property key in dot notation
   * @return the value, or null if not found
   */
  public static String get(String key) {
    if (key == null || key.isEmpty()) {
      return null;
    }

    // 1. Check system property
    String value = System.getProperty(key);
    if (value != null && !value.isEmpty()) {
      return value;
    }

    // 2. Check environment variable (convert to UPPER_SNAKE_CASE)
    String envKey = key.replace('.', '_').replace('-', '_').toUpperCase();
    value = System.getenv(envKey);
    if (value != null && !value.isEmpty()) {
      return value;
    }

    return null;
  }
}
