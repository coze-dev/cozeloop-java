package com.coze.loop.prompt;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Jinja2TemplateEngine.
 */
class Jinja2TemplateEngineTest {

    private final Jinja2TemplateEngine engine = new Jinja2TemplateEngine();

    @Test
    void testRenderWithSimpleVariable() {
        String template = "Hello {{ name }}!";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("Hello Alice!");
    }

    @Test
    void testRenderWithConditional() {
        String template = "{% if active %}Active{% else %}Inactive{% endif %}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("active", true);
        
        String result = engine.render(template, variables);
        
        assertThat(result.trim()).isEqualTo("Active");
    }

    @Test
    void testRenderWithLoop() {
        String template = "{% for item in items %}{{ item }} {% endfor %}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("items", java.util.Arrays.asList("a", "b", "c"));
        
        String result = engine.render(template, variables);
        
        assertThat(result.trim()).isEqualTo("a b c");
    }

    @Test
    void testRenderWithNullTemplate() {
        Map<String, Object> variables = new HashMap<>();
        String result = engine.render(null, variables);
        assertThat(result).isNull();
    }

    @Test
    void testRenderWithEmptyTemplate() {
        Map<String, Object> variables = new HashMap<>();
        String result = engine.render("", variables);
        assertThat(result).isEmpty();
    }

    @Test
    void testRenderWithMissingVariable() {
        String template = "Hello {{ name }}!";
        Map<String, Object> variables = new HashMap<>();
        
        // Jinja2 with failOnUnknownTokens=false should handle missing variables gracefully
        String result = engine.render(template, variables);
        
        assertThat(result).isNotNull();
    }

    @Test
    void testRenderWithComplexExpression() {
        String template = "Count: {{ items|length }}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("items", java.util.Arrays.asList(1, 2, 3));
        
        String result = engine.render(template, variables);
        
        assertThat(result.trim()).isEqualTo("Count: 3");
    }

    @Test
    void testRenderWithNestedVariables() {
        String template = "User: {{ user.name }}, Age: {{ user.age }}";
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Bob");
        user.put("age", 30);
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        
        String result = engine.render(template, variables);
        
        assertThat(result.trim()).isEqualTo("User: Bob, Age: 30");
    }
}

