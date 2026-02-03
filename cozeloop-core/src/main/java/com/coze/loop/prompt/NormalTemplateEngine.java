package com.coze.loop.prompt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.coze.loop.exception.ErrorCode;
import com.coze.loop.exception.PromptException;

/**
 * Normal template engine using Apache Commons Text. Supports ${variable} and {{variable}}
 * placeholders.
 */
public class NormalTemplateEngine implements TemplateEngine {

  @Override
  public String render(String template, Map<String, Object> variables) {
    if (template == null || template.isEmpty()) {
      return template;
    }

    try {
      // Handle null variables
      if (variables == null) {
        variables = new HashMap<>();
      }

      // Convert all values to strings
      Map<String, String> stringVars = new HashMap<>();
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
        stringVars.put(entry.getKey(), value);
      }

      // Replace ${variable} style
      StringSubstitutor substitutor = new StringSubstitutor(stringVars);
      String result = substitutor.replace(template);

      // Replace {{variable}} style
      for (Map.Entry<String, String> entry : stringVars.entrySet()) {
        String placeholder = "{{" + entry.getKey() + "}}";
        result = result.replace(placeholder, entry.getValue());
      }

      return result;
    } catch (Exception e) {
      throw new PromptException(
          ErrorCode.TEMPLATE_RENDER_ERROR, "Failed to render normal template", e);
    }
  }
}
