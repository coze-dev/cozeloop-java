package com.coze.loop.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coze.loop.entity.*;
import com.coze.loop.exception.PromptException;

/** Unit tests for PromptFormatter. */
class PromptFormatterTest {

  private PromptFormatter formatter;

  @BeforeEach
  void setUp() {
    formatter = new PromptFormatter();
  }

  @Test
  void testFormatWithNormalTemplate() {
    Prompt prompt = createPrompt(TemplateType.NORMAL);
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "Alice");
    variables.put("message", "Hello");

    List<Message> messages = formatter.format(prompt, variables);

    assertThat(messages).isNotNull();
    assertThat(messages).hasSize(1);
    assertThat(messages.get(0).getContent()).contains("Alice");
    assertThat(messages.get(0).getContent()).contains("Hello");
  }

  @Test
  void testFormatWithJinja2Template() {
    Prompt prompt = createPrompt(TemplateType.JINJA2);
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "Bob");

    List<Message> messages = formatter.format(prompt, variables);

    assertThat(messages).isNotNull();
    assertThat(messages).hasSize(1);
  }

  @Test
  void testFormatWithNullTemplate() {
    Prompt prompt = new Prompt();
    prompt.setPromptTemplate(null);
    Map<String, Object> variables = new HashMap<>();

    assertThatThrownBy(() -> formatter.format(prompt, variables))
        .isInstanceOf(PromptException.class);
  }

  @Test
  void testFormatWithNullPrompt() {
    Map<String, Object> variables = new HashMap<>();

    assertThatThrownBy(() -> formatter.format(null, variables)).isInstanceOf(PromptException.class);
  }

  @Test
  void testFormatWithNullVariables() {
    Prompt prompt = createPrompt(TemplateType.NORMAL);

    List<Message> messages = formatter.format(prompt, null);

    assertThat(messages).isNotNull();
  }

  @Test
  void testFormatWithEmptyVariables() {
    Prompt prompt = createPrompt(TemplateType.NORMAL);
    Map<String, Object> variables = new HashMap<>();

    List<Message> messages = formatter.format(prompt, variables);

    assertThat(messages).isNotNull();
  }

  @Test
  void testFormatWithMultipleMessages() {
    Prompt prompt = createPromptWithMultipleMessages();
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "Charlie");

    List<Message> messages = formatter.format(prompt, variables);

    assertThat(messages).isNotNull();
    assertThat(messages.size()).isGreaterThan(1);
  }

  @Test
  void testFormatDoesNotModifyOriginal() {
    Prompt prompt = createPrompt(TemplateType.NORMAL);
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "David");

    String originalContent = prompt.getPromptTemplate().getMessages().get(0).getContent();
    List<Message> messages = formatter.format(prompt, variables);

    // Original should remain unchanged (deep copy)
    assertThat(prompt.getPromptTemplate().getMessages().get(0).getContent())
        .isEqualTo(originalContent);
    assertThat(messages.get(0).getContent()).isNotEqualTo(originalContent);
  }

  @Test
  void testFormatWithDefaultTemplateType() {
    Prompt prompt = createPrompt(null); // null template type
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", "Eve");

    List<Message> messages = formatter.format(prompt, variables);

    assertThat(messages).isNotNull();
  }

  private Prompt createPrompt(TemplateType templateType) {
    PromptTemplate template = new PromptTemplate();
    template.setTemplateType(templateType);

    List<Message> messages = new ArrayList<>();
    Message message = new Message(Role.USER);
    message.setContent("Hello ${name}, your message is: ${message}");
    messages.add(message);
    template.setMessages(messages);

    Prompt prompt = new Prompt();
    prompt.setPromptTemplate(template);
    return prompt;
  }

  private Prompt createPromptWithMultipleMessages() {
    PromptTemplate template = new PromptTemplate();
    template.setTemplateType(TemplateType.NORMAL);

    List<Message> messages = new ArrayList<>();
    Message msg1 = new Message(Role.SYSTEM);
    msg1.setContent("System: ${name}");
    messages.add(msg1);

    Message msg2 = new Message(Role.USER);
    msg2.setContent("User: ${name}");
    messages.add(msg2);

    template.setMessages(messages);

    Prompt prompt = new Prompt();
    prompt.setPromptTemplate(template);
    return prompt;
  }
}
