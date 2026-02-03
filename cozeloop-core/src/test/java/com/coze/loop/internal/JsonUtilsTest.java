package com.coze.loop.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

/** Unit tests for JsonUtils. */
class JsonUtilsTest {

  @Test
  void testToJsonWithObject() {
    Map<String, Object> map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", 123);

    String json = JsonUtils.toJson(map);

    assertThat(json).isNotNull();
    assertThat(json).contains("key1");
    assertThat(json).contains("value1");
    assertThat(json).contains("key2");
    assertThat(json).contains("123");
  }

  @Test
  void testToJsonWithNull() {
    String json = JsonUtils.toJson(null);
    assertThat(json).isNull();
  }

  @Test
  void testToJsonWithString() {
    String input = "test string";
    String json = JsonUtils.toJson(input);
    assertThat(json).isEqualTo(input);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testFromJsonWithMap() {
    String json = "{\"key1\":\"value1\",\"key2\":123}";
    Map<String, Object> result = JsonUtils.fromJson(json, Map.class);

    assertThat(result).isNotNull();
    assertThat(result.get("key1")).isEqualTo("value1");
    assertThat(result.get("key2")).isEqualTo(123);
  }

  @Test
  void testFromJsonWithNull() {
    String result = JsonUtils.fromJson(null, String.class);
    assertThat(result).isNull();
  }

  @Test
  void testFromJsonWithEmptyString() {
    String result = JsonUtils.fromJson("", String.class);
    assertThat(result).isNull();
  }

  @Test
  void testFromJsonWithTypeReference() {
    String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    Map<String, String> result =
        JsonUtils.fromJson(json, new TypeReference<Map<String, String>>() {});

    assertThat(result).isNotNull();
    assertThat(result.get("key1")).isEqualTo("value1");
    assertThat(result.get("key2")).isEqualTo("value2");
  }

  @Test
  void testFromJsonWithInvalidJson() {
    String invalidJson = "{invalid json}";

    assertThatThrownBy(() -> JsonUtils.fromJson(invalidJson, Map.class))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to parse JSON");
  }

  @Test
  void testGetMapper() {
    assertThat(JsonUtils.getMapper()).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testRoundTrip() {
    Map<String, Object> original = new HashMap<>();
    original.put("name", "test");
    original.put("age", 25);
    original.put("active", true);

    String json = JsonUtils.toJson(original);
    Map<String, Object> restored = JsonUtils.fromJson(json, Map.class);

    assertThat(restored).isEqualTo(original);
  }
}
