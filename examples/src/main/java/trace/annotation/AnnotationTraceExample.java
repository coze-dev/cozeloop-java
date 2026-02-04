package trace.annotation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.coze.loop.spring.annotation.CozeTrace;

/**
 * Example demonstrating how to use the @CozeTrace annotation for automatic tracing.
 *
 * <p>This example uses the spring-boot-starter module, which automatically configures the
 * CozeTraceAspect to handle the @CozeTrace annotation.
 */
@SpringBootApplication(scanBasePackages = {"com.coze.loop.spring", "trace.annotation"})
public class AnnotationTraceExample {

  @CozeTrace(name = "root")
  public static void main(String[] args) {
    // Set environment variables for the example
    // In a real application, these would be in application.yml or environment variables
    // workspaceId and apiToken from property are set to client automatically, you also can set in
    // application.yml
    System.setProperty("cozeloop.workspace-id", "****");
    System.setProperty("cozeloop.auth.token", "pat_***");

    ConfigurableApplicationContext context =
        SpringApplication.run(AnnotationTraceExample.class, args);

    try {
      LlmService service = context.getBean(LlmService.class);
      service.rootTrace(context);
    } finally {
      context.close();
    }
  }

  @Service
  public static class LlmService {

    /** Basic usage: Automatically captures method name as span name. */
    @CozeTrace
    public void rootTrace(ConfigurableApplicationContext context) {
      try {
        LlmService service = context.getBean(LlmService.class);

        System.out.println("--- Running simple trace ---");
        service.simpleTrace("Hello, Coze!");

        System.out.println("\n--- Running trace with custom span info ---");
        service.customTrace("GPT-4", "What is Java?");

        System.out.println("\n--- Running trace with SpEL expressions ---");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", "user_123");
        service.spelTrace("summarize", params);

        System.out.println("\n--- Running trace that fails ---");
        try {
          service.errorTrace();
        } catch (Exception e) {
          System.out.println("Caught expected error: " + e.getMessage());
        }
      } catch (BeansException e) {
        throw new RuntimeException(e);
      }
    }

    /** Basic usage: Automatically captures method name as span name. */
    @CozeTrace
    public void simpleTrace(String input) {
      System.out.println("Processing simple trace: " + input);
    }

    /** Customization: Specify span name, type, and capture input/output. */
    @CozeTrace(name = "llm_chat_v1", spanType = "model", captureArgs = true, captureReturn = true)
    public String customTrace(String model, String query) {
      System.out.println("Processing custom trace with model: " + model);
      return "Response for " + query;
    }

    /**
     * Advanced usage: Use SpEL expressions for dynamic span names and precise data extraction.
     * #arg0, #arg1 refers to method arguments. #args[n] also works.
     */
    @CozeTrace(
        name = "operation_#{#arg0}",
        spanType = "business",
        inputExpression = "#arg1",
        outputExpression = "#result")
    public Map<String, Object> spelTrace(String action, Map<String, Object> params) {
      System.out.println("Processing dynamic trace for action: " + action);
      Map<String, Object> result = new HashMap<>();
      result.put("status", "success");
      result.put("action_performed", action);
      return result;
    }

    /**
     * Error handling: The aspect automatically captures exceptions and records them as span errors.
     */
    @CozeTrace(name = "unstable_operation")
    public void errorTrace() {
      throw new RuntimeException("Simulation of an internal error");
    }
  }
}
