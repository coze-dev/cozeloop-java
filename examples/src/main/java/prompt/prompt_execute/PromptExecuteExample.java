package prompt.prompt_execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.entity.ExecuteParam;
import com.coze.loop.entity.ExecuteResult;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Role;
import com.coze.loop.internal.JsonUtils;
import com.coze.loop.stream.StreamReader;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * Example demonstrating how to use the execute and executeStreaming methods.
 *
 * <p>Reference: Golang SDK ptaas example.
 */
public class PromptExecuteExample {

  public static void main(String[] args) {
    // Set environment variables for the example
    String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
    String apiToken = System.getenv("COZELOOP_API_TOKEN");
    String promptKey = System.getenv("COZELOOP_PROMPT_KEY");

    if (workspaceId == null || apiToken == null) {
      System.err.println("请设置环境变量：");
      System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
      System.err.println("  COZELOOP_API_TOKEN=your_token");
      System.exit(1);
    }

    // 1. Initialize client
    // Builder automatically picks up workspace-id and token from system properties
    try (CozeLoopClient client =
        new CozeLoopClientBuilder().workspaceId(workspaceId).tokenAuth(apiToken).build()) {

      // 2. Start a root span for the entire operation
      try (CozeLoopSpan span = client.startSpan("prompt_execute_demo", "custom")) {
        span.setUserIDBaggage("userid-123456789");

        // 3. Prepare execution parameters
        Map<String, Object> variableVals = new HashMap<>();
        variableVals.put("name", "Mobile Problem Answering Tool");
        variableVals.put("platform", "Android");

        List<Message> additionalMessages = new ArrayList<>();
        Message userMsg = new Message();
        userMsg.setRole(Role.USER);
        userMsg.setContent("Keep the answer brief.");
        additionalMessages.add(userMsg);

        ExecuteParam executeParam =
            ExecuteParam.builder()
                .promptKey(promptKey)
                .version("0.0.1")
                .variableVals(variableVals)
                .messages(additionalMessages)
                .build();

        // 4. Non-streaming execution
        System.out.println("--- Starting Non-Streaming Execution ---");
        executeNonStream(client, executeParam);

        // 5. Streaming execution
        System.out.println("\n--- Starting Streaming Execution ---");
        executeStream(client, executeParam);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void executeNonStream(CozeLoopClient client, ExecuteParam param) {
    try {
      ExecuteResult result = client.execute(param);
      printExecuteResult(result);
    } catch (Exception e) {
      System.err.println("Non-streaming execution failed: " + e.getMessage());
    }
  }

  private static void executeStream(CozeLoopClient client, ExecuteParam param) {
    try (StreamReader<ExecuteResult> reader = client.executeStreaming(param)) {
      ExecuteResult result;
      while ((result = reader.recv()) != null) {
        printExecuteResult(result);
      }
      System.out.println("\nStream finished.");
    } catch (Exception e) {
      System.err.println("Streaming execution failed: " + e.getMessage());
    }
  }

  private static void printExecuteResult(ExecuteResult result) {
    if (result.getMessage() != null) {
      System.out.println("Message: " + JsonUtils.toJson(result.getMessage()));
    }
    if (result.getFinishReason() != null) {
      System.out.println("FinishReason: " + result.getFinishReason());
    }
    if (result.getUsage() != null) {
      System.out.println("Usage: " + JsonUtils.toJson(result.getUsage()));
    }
  }
}
