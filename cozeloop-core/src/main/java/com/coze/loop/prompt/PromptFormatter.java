package com.coze.loop.prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.coze.loop.entity.*;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.exception.PromptException;
import com.coze.loop.internal.CozeLoopLogger;

/** Formatter for prompt messages. Handles template rendering and variable substitution. */
public class PromptFormatter {
  private static final Logger logger = CozeLoopLogger.getLogger(PromptFormatter.class);

  private final TemplateEngine normalEngine;
  private final TemplateEngine jinja2Engine;
  private final VariableValidator validator;

  public PromptFormatter() {
    this.normalEngine = new NormalTemplateEngine();
    this.jinja2Engine = new Jinja2TemplateEngine();
    this.validator = new VariableValidator();
  }

  /**
   * Format prompt messages with variables.
   *
   * @param prompt the prompt
   * @param variables the variables to substitute
   * @return formatted messages
   */
  public List<Message> format(Prompt prompt, Map<String, Object> variables) {
    if (prompt == null || prompt.getPromptTemplate() == null) {
      throw new PromptException(ErrorCode.INVALID_PARAM, "Prompt or template is null");
    }

    PromptTemplate template = prompt.getPromptTemplate();

    // Validate variables
    if (template.getVariableDefs() != null) {
      validator.validate(variables, template.getVariableDefs());
    }

    // Deep copy messages to avoid modifying original
    List<Message> messages = new ArrayList<>();
    if (template.getMessages() != null) {
      for (Message msg : template.getMessages()) {
        messages.add(msg.deepCopy());
      }
    }

    // Determine template type
    TemplateType templateType = template.getTemplateType();
    if (templateType == null) {
      templateType = TemplateType.NORMAL;
    }

    // Select appropriate engine
    TemplateEngine engine = templateType == TemplateType.JINJA2 ? jinja2Engine : normalEngine;

    // Format each message
    for (Message message : messages) {
      formatMessage(message, variables, engine);
    }

    return messages;
  }

  /** Format a single message. */
  private void formatMessage(
      Message message, Map<String, Object> variables, TemplateEngine engine) {
    // Format content
    if (message.getContent() != null && !message.getContent().isEmpty()) {
      String formatted = engine.render(message.getContent(), variables);
      message.setContent(formatted);
    }

    // Format parts
    if (message.getParts() != null) {
      for (ContentPart part : message.getParts()) {
        formatContentPart(part, variables, engine);
      }
    }
  }

  /** Format a content part. */
  private void formatContentPart(
      ContentPart part, Map<String, Object> variables, TemplateEngine engine) {
    if (part.getType() == ContentType.TEXT && part.getText() != null) {
      String formatted = engine.render(part.getText(), variables);
      part.setText(formatted);
    } else if (part.getType() == ContentType.MULTI_PART_VARIABLE) {
      // Handle multi-part variable
      // This is a placeholder for a variable that should be expanded
      // The actual implementation depends on your requirements
      logger.debug("Multi-part variable found in content part");
    }
  }
}
