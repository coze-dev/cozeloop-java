package com.coze.loop.prompt;

import java.util.Map;

/**
 * Template engine interface for rendering prompt templates.
 */
public interface TemplateEngine {
    /**
     * Render a template with variables.
     *
     * @param template the template string
     * @param variables the variables to substitute
     * @return rendered string
     */
    String render(String template, Map<String, Object> variables);
}

