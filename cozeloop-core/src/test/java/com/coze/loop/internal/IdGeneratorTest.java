package com.coze.loop.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for IdGenerator.
 */
class IdGeneratorTest {

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-f]+$");

    @Test
    void testGenerateTraceId() {
        String traceId = IdGenerator.generateTraceId();
        
        assertThat(traceId).isNotNull();
        assertThat(traceId).hasSize(32);
        assertThat(traceId).matches(HEX_PATTERN);
    }

    @Test
    void testGenerateSpanId() {
        String spanId = IdGenerator.generateSpanId();
        
        assertThat(spanId).isNotNull();
        assertThat(spanId).hasSize(16);
        assertThat(spanId).matches(HEX_PATTERN);
    }

    @Test
    void testGenerateHexString() {
        String hex = IdGenerator.generateHexString(8);
        
        assertThat(hex).isNotNull();
        assertThat(hex).hasSize(8);
        assertThat(hex).matches(HEX_PATTERN);
    }

    @Test
    void testGenerateHexStringWithDifferentLengths() {
        assertThat(IdGenerator.generateHexString(1)).hasSize(1);
        assertThat(IdGenerator.generateHexString(10)).hasSize(10);
        assertThat(IdGenerator.generateHexString(64)).hasSize(64);
    }

    @Test
    void testGenerateUuid() {
        String uuid = IdGenerator.generateUuid();
        
        assertThat(uuid).isNotNull();
        assertThat(uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @RepeatedTest(10)
    void testTraceIdUniqueness() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateTraceId();
            assertThat(ids).doesNotContain(id);
            ids.add(id);
        }
    }

    @RepeatedTest(10)
    void testSpanIdUniqueness() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateSpanId();
            assertThat(ids).doesNotContain(id);
            ids.add(id);
        }
    }

    @RepeatedTest(10)
    void testUuidUniqueness() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateUuid();
            assertThat(ids).doesNotContain(id);
            ids.add(id);
        }
    }
}

