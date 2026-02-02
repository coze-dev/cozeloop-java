package com.coze.loop.spring.integration;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.spring.annotation.CozeTrace;
import com.coze.loop.spring.aop.CozeTraceAspect;
import com.coze.loop.spring.config.CozeLoopProperties;
import com.coze.loop.spring.test.OpenTelemetryTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CozeLoop Spring Boot Starter.
 */
@SpringBootTest(classes = CozeLoopSpringBootIntegrationTest.TestApp.class)
@TestPropertySource(properties = {
    "cozeloop.workspace-id=test-workspace-integration",
    "cozeloop.service-name=test-service",
    "cozeloop.auth.token=test-token-123",
    "cozeloop.trace.enabled=true"
})
class CozeLoopSpringBootIntegrationTest {

    @Autowired(required = false)
    private CozeLoopClient cozeLoopClient;

    @Autowired(required = false)
    private CozeTraceAspect cozeTraceAspect;

    @Autowired(required = false)
    private CozeLoopProperties properties;

    @Autowired(required = false)
    private TestService testService;

    @BeforeEach
    void setUp() {
        OpenTelemetryTestUtils.resetGlobalOpenTelemetry();
    }

    @Test
    void testBeansAreCreated() {
        assertThat(cozeLoopClient).isNotNull();
        assertThat(cozeTraceAspect).isNotNull();
        assertThat(properties).isNotNull();
        assertThat(testService).isNotNull();
    }

    @Test
    void testPropertiesAreLoaded() {
        assertThat(properties.getWorkspaceId()).isEqualTo("test-workspace-integration");
        assertThat(properties.getServiceName()).isEqualTo("test-service");
        assertThat(properties.getAuth().getToken()).isEqualTo("test-token-123");
    }

    @Test
    void testCozeTraceAnnotationWorks() {
        String result = testService.tracedMethod("test-input");
        assertThat(result).isEqualTo("result: test-input");
    }

    @Test
    void testCozeTraceWithSpelExpression() {
        String result = testService.tracedMethodWithSpel("test");
        assertThat(result).isNotNull();
    }

    @SpringBootApplication
    @Import(com.coze.loop.spring.autoconfigure.CozeLoopAutoConfiguration.class)
    static class TestApp {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    /**
     * Test service with @CozeTrace annotation.
     */
    static class TestService {

        @CozeTrace(name = "test-method", spanType = "custom", captureArgs = true, captureReturn = true)
        public String tracedMethod(String input) {
            return "result: " + input;
        }

        @CozeTrace(name = "#{'span_' + #args[0]}", spanType = "llm")
        public String tracedMethodWithSpel(String input) {
            return "spel-result: " + input;
        }

        @CozeTrace(inputExpression = "#args[0]", outputExpression = "#result")
        public String tracedMethodWithExpressions(String input) {
            return "expression-result: " + input;
        }
    }
}

