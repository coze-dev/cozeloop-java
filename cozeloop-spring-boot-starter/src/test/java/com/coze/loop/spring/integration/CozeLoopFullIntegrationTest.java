package com.coze.loop.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.spring.annotation.CozeTrace;
import com.coze.loop.spring.test.OpenTelemetryTestUtils;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * Full integration test for CozeLoop Spring Boot Starter. Tests the complete flow including tracing
 * and prompt operations.
 */
@SpringBootTest(classes = CozeLoopFullIntegrationTest.FullTestApp.class)
@TestPropertySource(
    properties = {
      "cozeloop.workspace-id=test-workspace-full",
      "cozeloop.service-name=full-test-service",
      "cozeloop.auth.token=test-token-full",
      "cozeloop.trace.enabled=true",
      "cozeloop.http.connect-timeout-seconds=10",
      "cozeloop.http.read-timeout-seconds=30",
      "cozeloop.trace.max-queue-size=1024",
      "cozeloop.prompt.cache.max-size=500"
    })
class CozeLoopFullIntegrationTest {

  @Autowired private CozeLoopClient client;

  @Autowired private FullTestService testService;

  @BeforeEach
  void setUp() {
    OpenTelemetryTestUtils.resetGlobalOpenTelemetry();
  }

  @Test
  void testClientIsAvailable() {
    assertThat(client).isNotNull();
    assertThat(client.getWorkspaceId()).isEqualTo("test-workspace-full");
  }

  @Test
  void testSpanCreation() {
    try (CozeLoopSpan span = client.startSpan("test-span", "custom")) {
      span.setInput("test-input");
      span.setOutput("test-output");
      span.setStatusCode(0);

      assertThat(span).isNotNull();
    }
  }

  @Test
  void testTraceAnnotationIntegration() {
    String result = testService.processWithTrace("input-data");

    assertThat(result).isNotNull();
    assertThat(result).contains("processed");
  }

  @Test
  void testTraceAnnotationWithErrorHandling() {
    try {
      testService.processWithError("test");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).contains("test error");
    }
  }

  @Test
  void testTraceAnnotationWithSpel() {
    String result = testService.processWithSpel("test-value", 123);

    assertThat(result).isNotNull();
  }

  @Test
  void testMultipleTracedMethods() {
    String result1 = testService.processWithTrace("input1");
    String result2 = testService.processWithTrace("input2");

    assertThat(result1).isNotNull();
    assertThat(result2).isNotNull();
  }

  @SpringBootApplication
  @Import(com.coze.loop.spring.autoconfigure.CozeLoopAutoConfiguration.class)
  static class FullTestApp {
    @Bean
    public FullTestService fullTestService() {
      return new FullTestService();
    }
  }

  /** Service for full integration testing. */
  static class FullTestService {

    @CozeTrace(
        name = "process-method",
        spanType = "custom",
        captureArgs = true,
        captureReturn = true)
    public String processWithTrace(String input) {
      return "processed: " + input;
    }

    @CozeTrace(name = "error-method", spanType = "custom")
    public String processWithError(String input) {
      throw new RuntimeException("test error: " + input);
    }

    @CozeTrace(
        name = "#{'spel_' + #args[0] + '_' + #args[1]}",
        spanType = "llm",
        inputExpression = "#args[0]",
        outputExpression = "#result")
    public String processWithSpel(String value, int number) {
      return "spel-result: " + value + "-" + number;
    }

    @CozeTrace(name = "multi-arg-method", captureArgs = true)
    public String processMultipleArgs(String arg1, int arg2, boolean arg3) {
      return "multi: " + arg1 + "-" + arg2 + "-" + arg3;
    }
  }
}
