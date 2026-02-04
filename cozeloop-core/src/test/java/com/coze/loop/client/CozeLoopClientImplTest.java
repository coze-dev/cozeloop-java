package com.coze.loop.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.http.HttpClient;
import com.coze.loop.prompt.GetPromptParam;
import com.coze.loop.prompt.PromptProvider;
import com.coze.loop.trace.CozeLoopSpan;
import com.coze.loop.trace.CozeLoopTracerProvider;

import io.opentelemetry.api.trace.Tracer;

/** Unit tests for CozeLoopClientImpl. */
@ExtendWith(MockitoExtension.class)
class CozeLoopClientImplTest {

  @Mock private CozeLoopTracerProvider tracerProvider;

  @Mock private PromptProvider promptProvider;

  @Mock private HttpClient httpClient;

  @Mock private Tracer tracer;

  private CozeLoopClientImpl client;
  private String workspaceId = "test-workspace";

  @BeforeEach
  void setUp() {
    when(tracerProvider.getTracer(any(), any())).thenReturn(tracer);
    client = new CozeLoopClientImpl(workspaceId, tracerProvider, promptProvider, httpClient);
  }

  @Test
  void testGetWorkspaceId() {
    assertThat(client.getWorkspaceId()).isEqualTo(workspaceId);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testStartSpan() {
    io.opentelemetry.api.trace.Span span = mock(io.opentelemetry.api.trace.Span.class);
    io.opentelemetry.api.trace.SpanBuilder spanBuilder =
        mock(io.opentelemetry.api.trace.SpanBuilder.class);
    io.opentelemetry.context.Scope scope = mock(io.opentelemetry.context.Scope.class);

    when(tracer.spanBuilder(any())).thenReturn(spanBuilder);
    when(spanBuilder.setAttribute(any(io.opentelemetry.api.common.AttributeKey.class), any()))
        .thenReturn(spanBuilder);
    when(spanBuilder.startSpan()).thenReturn(span);
    when(span.makeCurrent()).thenReturn(scope);

    CozeLoopSpan result = client.startSpan("test-span");

    assertThat(result).isNotNull();
    verify(tracer).spanBuilder("test-span");
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testStartSpanWithType() {
    io.opentelemetry.api.trace.Span span = mock(io.opentelemetry.api.trace.Span.class);
    io.opentelemetry.api.trace.SpanBuilder spanBuilder =
        mock(io.opentelemetry.api.trace.SpanBuilder.class);
    io.opentelemetry.context.Scope scope = mock(io.opentelemetry.context.Scope.class);

    when(tracer.spanBuilder(any())).thenReturn(spanBuilder);
    when(spanBuilder.setAttribute(any(io.opentelemetry.api.common.AttributeKey.class), any()))
        .thenReturn(spanBuilder);
    when(spanBuilder.startSpan()).thenReturn(span);
    when(span.makeCurrent()).thenReturn(scope);

    CozeLoopSpan result = client.startSpan("test-span", "llm");

    assertThat(result).isNotNull();
    verify(spanBuilder)
        .setAttribute(any(io.opentelemetry.api.common.AttributeKey.class), eq("llm"));
  }

  @Test
  void testGetPrompt() {
    GetPromptParam param = GetPromptParam.builder().promptKey("test-key").build();
    Prompt prompt = new Prompt();
    prompt.setPromptKey("test-key");

    when(promptProvider.getPrompt(param)).thenReturn(prompt);

    Prompt result = client.getPrompt(param);

    assertThat(result).isNotNull();
    assertThat(result.getPromptKey()).isEqualTo("test-key");
    verify(promptProvider).getPrompt(param);
  }

  @Test
  void testFormatPrompt() {
    Prompt prompt = new Prompt();
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "test");

    List<Message> messages = new ArrayList<>();
    Message message = new Message();
    message.setContent("Hello test");
    messages.add(message);

    when(promptProvider.formatPrompt(prompt, variables)).thenReturn(messages);

    List<Message> result = client.formatPrompt(prompt, variables);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(promptProvider).formatPrompt(prompt, variables);
  }

  @Test
  void testGetAndFormatPrompt() {
    GetPromptParam param = GetPromptParam.builder().promptKey("test-key").build();
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "test");

    List<Message> messages = new ArrayList<>();
    Message message = new Message();
    message.setContent("Hello test");
    messages.add(message);

    when(promptProvider.getAndFormat(param, variables)).thenReturn(messages);

    List<Message> result = client.getAndFormatPrompt(param, variables);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(promptProvider).getAndFormat(param, variables);
  }

  @Test
  void testInvalidatePromptCache() {
    GetPromptParam param = GetPromptParam.builder().promptKey("test-key").build();

    client.invalidatePromptCache(param);

    verify(promptProvider).invalidateCache(param);
  }

  @Test
  void testShutdown() {
    client.shutdown();

    verify(tracerProvider).shutdown();
    verify(httpClient).close();
  }

  @Test
  void testClose() {
    client.close();

    verify(tracerProvider).shutdown();
    verify(httpClient).close();
  }

  @Test
  void testShutdownMultipleTimes() {
    client.shutdown();
    client.shutdown();

    // Should only shutdown once
    verify(tracerProvider, times(1)).shutdown();
    verify(httpClient, times(1)).close();
  }

  @Test
  void testOperationsAfterShutdown() {
    client.shutdown();

    assertThatThrownBy(() -> client.getWorkspaceId())
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CLIENT_CLOSED);
            });
  }

  @Test
  void testStartSpanAfterShutdown() {
    client.shutdown();

    assertThatThrownBy(() -> client.startSpan("test"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CLIENT_CLOSED);
            });
  }

  @Test
  void testGetPromptAfterShutdown() {
    client.shutdown();
    GetPromptParam param = GetPromptParam.builder().promptKey("test-key").build();

    assertThatThrownBy(() -> client.getPrompt(param))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CLIENT_CLOSED);
            });
  }
}
