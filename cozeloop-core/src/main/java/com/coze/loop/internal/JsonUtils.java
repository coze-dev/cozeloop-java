package com.coze.loop.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/** JSON utility class for serialization and deserialization. */
public final class JsonUtils {
  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

  private JsonUtils() {
    // Utility class
  }

  /**
   * Convert object to JSON string.
   *
   * @param obj the object to convert
   * @return JSON string
   */
  public static String toJson(Object obj) {
    if (obj == null) {
      return null;
    }
    if (obj instanceof String) {
      return (String) obj;
    }
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  /**
   * Parse JSON string to object.
   *
   * @param json JSON string
   * @param clazz target class
   * @param <T> type parameter
   * @return parsed object
   */
  public static <T> T fromJson(String json, Class<T> clazz) {
    if (json == null || json.isEmpty()) {
      return null;
    }
    try {
      return MAPPER.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to parse JSON to " + clazz.getName(), e);
    }
  }

  /**
   * Parse JSON string to object with TypeReference.
   *
   * @param json JSON string
   * @param typeRef type reference
   * @param <T> type parameter
   * @return parsed object
   */
  public static <T> T fromJson(String json, TypeReference<T> typeRef) {
    if (json == null || json.isEmpty()) {
      return null;
    }
    try {
      return MAPPER.readValue(json, typeRef);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to parse JSON", e);
    }
  }

  /**
   * Get the ObjectMapper instance.
   *
   * @return ObjectMapper
   */
  public static ObjectMapper getMapper() {
    return MAPPER;
  }
}
