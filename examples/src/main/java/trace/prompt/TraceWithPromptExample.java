package trace.prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.entity.Role;
import com.coze.loop.prompt.GetPromptParam;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * 追踪与提示结合示例
 *
 * <p>展示在追踪过程中使用 prompt，包括： - 在追踪过程中获取 prompt - 格式化 prompt - 与 LLM 调用的完整流程
 *
 * <p>使用前请先设置以下环境变量： - COZELOOP_WORKSPACE_ID: 你的工作空间 ID - COZELOOP_API_TOKEN: 你的访问令牌
 *
 * <p>注意：需要在平台上创建一个 Prompt（Prompt Key 设置为 'prompt_hub_demo'）， 并在模板中添加以下消息，然后提交版本： - System: You are
 * a helpful bot, the conversation topic is {{var1}}. - Placeholder: placeholder1 - User: My
 * question is {{var2}} - Placeholder: placeholder2
 */
public class TraceWithPromptExample {

  public static void main(String[] args) {
    String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
    String apiToken = System.getenv("COZELOOP_API_TOKEN");
    String promptKey = System.getenv("COZELOOP_PROMPT_KEY");

    if (workspaceId == null || apiToken == null) {
      System.err.println("请设置环境变量：");
      System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
      System.err.println("  COZELOOP_API_TOKEN=your_token");
      System.exit(1);
    }

    // workspaceId and apiToken from env are set to client automatically
    CozeLoopClient client = new CozeLoopClientBuilder().build();

    try {
      // 1. 创建根 span
      try (CozeLoopSpan rootSpan = client.startSpan("root_span", "main_span")) {

        // 2. 获取 prompt
        Prompt prompt =
            client.getPrompt(
                GetPromptParam.builder()
                    .promptKey(promptKey)
                    .version("0.0.1") // 如果不指定版本，将获取对应 prompt 的最新版本
                    .build());

        if (prompt == null) {
          System.err.println("获取 prompt 失败：prompt 不存在");
          return;
        }

        // 3. 打印 prompt 信息
        if (prompt.getPromptTemplate() != null
            && prompt.getPromptTemplate().getMessages() != null) {
          System.out.println("Prompt 消息数量: " + prompt.getPromptTemplate().getMessages().size());
        }

        if (prompt.getLlmConfig() != null) {
          System.out.println("Prompt LLM 配置: " + prompt.getLlmConfig());
        }

        // 4. 格式化 prompt 消息
        String userMessageContent = "Hello!";
        String assistantMessageContent = "Hello!";

        // 准备变量
        Map<String, Object> variables = new HashMap<>();
        // 普通变量类型应该是 String
        variables.put("var1", "artificial intelligence");
        variables.put("var2", "What is AI?");

        // Placeholder 变量类型应该是 Message/List<Message>
        List<Message> placeholder1 = new ArrayList<>();
        placeholder1.add(Message.builder().role(Role.USER).content(userMessageContent).build());
        placeholder1.add(
            Message.builder().role(Role.ASSISTANT).content(assistantMessageContent).build());
        variables.put("placeholder1", placeholder1);

        // 格式化 prompt
        List<Message> messages = client.formatPrompt(prompt, variables);

        System.out.println("格式化后的消息数量: " + messages.size());
        for (Message msg : messages) {
          System.out.println("  Role: " + msg.getRole() + ", Content: " + msg.getContent());
        }

        // 5. 调用 LLM
        callLLM(client, messages);
      }
      System.out.println("追踪与提示示例执行成功！");
    } catch (Exception e) {
      System.err.println("执行失败：" + e.getMessage());
      e.printStackTrace();
    } finally {
      // force flush trace to backend
      // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
      // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
      client.flush();
    }
  }

  /** 调用 LLM */
  private static void callLLM(CozeLoopClient client, List<Message> messages) {
    try (CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
      // 模拟 LLM 处理
      String modelName = "gpt-4o-2024-05-13";

      try {
        Thread.sleep(1000); // 模拟网络延迟
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }

      // 模拟响应
      String[] respChoices = {"Hello! Can I help you?"};
      int respPromptTokens = 11;
      int respCompletionTokens = 52;

      // 设置 span 属性
      span.setInput(messages);
      span.setOutput(respChoices);
      span.setModelProvider("openai");
      span.setModelName(modelName);
      span.setInputTokens(respPromptTokens);
      span.setOutputTokens(respCompletionTokens);

      // 设置首次响应时间
      long firstRespTime = System.currentTimeMillis() * 1000;
      span.setTag("start_time_first_resp", firstRespTime);

      System.out.println("LLM 调用完成");
      System.out.println("  输出: " + respChoices[0]);
      System.out.println("  模型: " + modelName);
    }
  }
}
