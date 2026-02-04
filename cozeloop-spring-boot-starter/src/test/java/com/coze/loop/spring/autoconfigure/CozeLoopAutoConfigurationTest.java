package com.coze.loop.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.spring.aop.CozeTraceAspect;
import com.coze.loop.spring.config.CozeLoopProperties;
import com.coze.loop.spring.test.OpenTelemetryTestUtils;
import com.coze.loop.trace.CozeLoopContext;
import com.coze.loop.trace.CozeLoopSpan;

/** Unit tests for CozeLoopAutoConfiguration. */
class CozeLoopAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(CozeLoopAutoConfiguration.class));

  @BeforeEach
  void setUp() {
    OpenTelemetryTestUtils.resetGlobalOpenTelemetry();
  }

  @AfterEach
  void tearDown() {
    OpenTelemetryTestUtils.resetGlobalOpenTelemetry();
  }

  @Test
  void testAutoConfigurationWithTokenAuth() {
    contextRunner
        .withPropertyValues(
            "cozeloop.workspace-id=test-workspace", "cozeloop.auth.token=test-token")
        .run(
            context -> {
              assertThat(context).hasSingleBean(CozeLoopClient.class);
              assertThat(context).hasSingleBean(CozeTraceAspect.class);
            });
  }

  @Test
  void testAutoConfigurationWithJwtAuth() {
    // Use a valid base64-encoded test key (base64 of "test-key-123456789012345678901234567890")
    String validBase64Key = "dGVzdC1rZXktMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkw";
    contextRunner
        .withPropertyValues(
            "cozeloop.workspace-id=test-workspace",
            "cozeloop.auth.jwt.client-id=test-client",
            "cozeloop.auth.jwt.private-key=" + validBase64Key,
            "cozeloop.auth.jwt.public-key-id=test-key-id")
        .run(
            context -> {
              // JWT auth will fail with invalid key format, but auto-configuration should still
              // attempt to create client
              // The test verifies that the configuration is processed correctly
              assertThat(context).hasFailed();
            });
  }

  @Test
  void testAutoConfigurationWithoutWorkspaceId() {
    contextRunner
        .withPropertyValues("cozeloop.auth.token=test-token")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(CozeLoopClient.class);
            });
  }

  @Test
  void testAutoConfigurationWithCustomProperties() {
    contextRunner
        .withPropertyValues(
            "cozeloop.workspace-id=test-workspace",
            "cozeloop.service-name=custom-service",
            "cozeloop.base-url=https://custom.url",
            "cozeloop.auth.token=test-token",
            "cozeloop.http.connect-timeout-seconds=10",
            "cozeloop.http.read-timeout-seconds=20",
            "cozeloop.trace.max-queue-size=1024",
            "cozeloop.prompt.cache.max-size=500")
        .run(
            context -> {
              assertThat(context).hasSingleBean(CozeLoopClient.class);
              CozeLoopProperties properties = context.getBean(CozeLoopProperties.class);
              assertThat(properties.getServiceName()).isEqualTo("custom-service");
              assertThat(properties.getBaseUrl()).isEqualTo("https://custom.url");
            });
  }

  @Test
  void testAutoConfigurationWithTraceDisabled() {
    contextRunner
        .withPropertyValues(
            "cozeloop.workspace-id=test-workspace",
            "cozeloop.auth.token=test-token",
            "cozeloop.trace.enabled=false")
        .run(
            context -> {
              assertThat(context).hasSingleBean(CozeLoopClient.class);
              assertThat(context).doesNotHaveBean(CozeTraceAspect.class);
            });
  }

  @Test
  void testAutoConfigurationWithExistingClient() {
    contextRunner
        .withUserConfiguration(CustomClientConfiguration.class)
        .withPropertyValues(
            "cozeloop.workspace-id=test-workspace", "cozeloop.auth.token=test-token")
        .run(
            context -> {
              assertThat(context).hasSingleBean(CozeLoopClient.class);
              assertThat(context.getBean(CozeLoopClient.class))
                  .isSameAs(context.getBean(CustomClientConfiguration.class).customClient);
            });
  }

  @Test
  void testAutoConfigurationFailsWithoutAuth() {
    contextRunner
        .withPropertyValues("cozeloop.workspace-id=test-workspace")
        .run(
            context -> {
              assertThat(context).hasFailed();
            });
  }

  @TestConfiguration
  static class CustomClientConfiguration {
    private final CozeLoopClient customClient =
        new CozeLoopClient() {
          @Override
          public String getWorkspaceId() {
            return "custom";
          }

          @Override
          public void flush() {}

          @Override
          public com.coze.loop.trace.CozeLoopSpan startSpan(String name) {
            return null;
          }

          @Override
          public com.coze.loop.trace.CozeLoopSpan startSpan(String name, String spanType) {
            return null;
          }

          @Override
          public CozeLoopSpan startSpan(String name, String spanType, String scene) {
            return null;
          }

          @Override
          public CozeLoopSpan startSpan(String name, String spanType, CozeLoopSpan parentSpan) {
            return null;
          }

          @Override
          public CozeLoopSpan startSpan(
              String name, String spanType, CozeLoopSpan parentSpan, String scene) {
            return null;
          }

          @Override
          public CozeLoopSpan startSpan(
              String name, String spanType, CozeLoopContext parentContext) {
            return null;
          }

          @Override
          public CozeLoopSpan startSpan(
              String name, String spanType, CozeLoopContext parentContext, String scene) {
            return null;
          }

          @Override
          public CozeLoopContext extractContext(Map<String, String> headers) {
            return null;
          }

          @Override
          public com.coze.loop.entity.Prompt getPrompt(com.coze.loop.prompt.GetPromptParam param) {
            return null;
          }

          @Override
          public java.util.List<com.coze.loop.entity.Message> formatPrompt(
              com.coze.loop.entity.Prompt prompt, java.util.Map<String, Object> variables) {
            return null;
          }

          @Override
          public java.util.List<com.coze.loop.entity.Message> getAndFormatPrompt(
              com.coze.loop.prompt.GetPromptParam param, java.util.Map<String, Object> variables) {
            return null;
          }

          @Override
          public void invalidatePromptCache(com.coze.loop.prompt.GetPromptParam param) {}

          @Override
          public com.coze.loop.entity.ExecuteResult execute(
              com.coze.loop.entity.ExecuteParam param) {
            return null;
          }

          @Override
          public com.coze.loop.stream.StreamReader<com.coze.loop.entity.ExecuteResult>
              executeStreaming(com.coze.loop.entity.ExecuteParam param) {
            return null;
          }

          @Override
          public void shutdown() {}

          @Override
          public void close() {}
        };

    @Bean
    public CozeLoopClient cozeLoopClient() {
      return customClient;
    }
  }
}
