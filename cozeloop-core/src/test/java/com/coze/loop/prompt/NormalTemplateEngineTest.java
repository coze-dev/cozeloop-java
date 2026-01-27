package com.coze.loop.prompt;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NormalTemplateEngine.
 */
class NormalTemplateEngineTest {

    private final NormalTemplateEngine engine = new NormalTemplateEngine();

    @Test
    void testRenderWithDollarPlaceholder() {
        String template = "Hello ${name}, welcome!";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("Hello Alice, welcome!");
    }

    @Test
    void testRenderWithDoubleBracePlaceholder() {
        String template = "Hello {{name}}, welcome!";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Bob");
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("Hello Bob, welcome!");
    }

    @Test
    void testRenderWithMultipleVariables() {
        String template = "User: ${user}, Age: ${age}, Active: ${active}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Charlie");
        variables.put("age", 25);
        variables.put("active", true);
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("User: Charlie, Age: 25, Active: true");
    }

    @Test
    void testRenderWithMixedPlaceholders() {
        String template = "Hello ${name}, your code is {{code}}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "David");
        variables.put("code", "ABC123");
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("Hello David, your code is ABC123");
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
    void testRenderWithNullVariables() {
        String template = "Hello ${name}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", null);
        
        String result = engine.render(template, variables);
        
        // Null values are converted to empty string
        assertThat(result).isEqualTo("Hello ");
    }

    @Test
    void testRenderWithMissingVariable() {
        String template = "Hello ${name}";
        Map<String, Object> variables = new HashMap<>();
        
        String result = engine.render(template, variables);
        
        // Should keep the placeholder if variable is missing
        assertThat(result).contains("${name}");
    }

    @Test
    void testRenderWithNestedPlaceholders() {
        String template = "Value: ${value}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("value", "${nested}");
        
        String result = engine.render(template, variables);
        
        assertThat(result).isEqualTo("Value: ${nested}");
    }

    @Test
    void testRenderWithComplexTypes() {
        String template = "Value: ${value}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("value", new HashMap<>());
        
        String result = engine.render(template, variables);
        
        assertThat(result).isNotNull();
        assertThat(result).contains("Value:");
    }
}

