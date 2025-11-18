package com.coze.loop.prompt;

import com.coze.loop.exception.ErrorCode;
import com.coze.loop.exception.PromptException;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;

import java.util.Map;

/**
 * Jinja2 template engine using JinJava library.
 */
public class Jinja2TemplateEngine implements TemplateEngine {
    private final Jinjava jinjava;
    
    public Jinja2TemplateEngine() {
        JinjavaConfig config = JinjavaConfig.newBuilder()
            .withFailOnUnknownTokens(false)
            .build();
        this.jinjava = new Jinjava(config);
    }
    
    @Override
    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        try {
            RenderResult result = jinjava.renderForResult(template, variables);
            
            if (result.hasErrors()) {
                throw new PromptException(ErrorCode.TEMPLATE_RENDER_ERROR,
                    "Jinja2 template render errors: " + result.getErrors());
            }
            
            return result.getOutput();
        } catch (PromptException e) {
            throw e;
        } catch (Exception e) {
            throw new PromptException(ErrorCode.TEMPLATE_RENDER_ERROR,
                "Failed to render Jinja2 template", e);
        }
    }
}

