package prompt.prompt_hub_jinja;

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

/**
 * Prompt Hub Jinja 模板示例
 *
 * <p>如果你想在 prompt 中使用 Jinja 模板，可以参考以下示例。
 *
 * <p>展示： - Jinja 模板的使用 - 各种变量类型的格式化（string, int, bool, float, object, array） - 复杂数据结构的处理
 *
 * <p>使用前请先设置以下环境变量： - COZELOOP_WORKSPACE_ID: 你的工作空间 ID - COZELOOP_API_TOKEN: 你的访问令牌
 *
 * <p>注意：需要在平台上创建一个 Prompt（Prompt Key 设置为 'prompt_hub_demo'）， 并在模板中添加以下消息，然后提交版本： - System: You are
 * a helpful bot, the conversation topic is {{var1}}. - Placeholder: placeholder1 - User: My
 * question is {{var2}} - Placeholder: placeholder2
 */
public class PromptHubJinjaExample {

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
      try (com.coze.loop.trace.CozeLoopSpan rootSpan = client.startSpan("root_span", "main_span")) {

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

        // 3. 准备各种类型的变量（用于 Jinja 模板）
        Map<String, Object> variables = new HashMap<>();

        // 字符串变量
        variables.put("var_string", "hi");

        // 整数变量
        variables.put("var_int", 5);

        // 布尔变量
        variables.put("var_bool", true);

        // 浮点数变量
        variables.put("var_float", 1.0);

        // 对象变量
        Map<String, Object> address = new HashMap<>();
        address.put("city", "beijing");
        address.put("street", "123 Main");

        List<String> hobbies = new ArrayList<>();
        hobbies.add("reading");
        hobbies.add("coding");

        Map<String, Object> person = new HashMap<>();
        person.put("name", "John");
        person.put("age", 30);
        person.put("hobbies", hobbies);
        person.put("address", address);
        variables.put("var_object", person);

        // 字符串数组
        List<String> stringArray = new ArrayList<>();
        stringArray.add("hello");
        stringArray.add("nihao");
        variables.put("var_array_string", stringArray);

        // 布尔数组
        List<Boolean> boolArray = new ArrayList<>();
        boolArray.add(true);
        boolArray.add(false);
        boolArray.add(true);
        variables.put("var_array_boolean", boolArray);

        // 整数数组
        List<Long> intArray = new ArrayList<>();
        intArray.add(1L);
        intArray.add(2L);
        intArray.add(3L);
        intArray.add(4L);
        variables.put("var_array_int", intArray);

        // 浮点数数组
        List<Double> floatArray = new ArrayList<>();
        floatArray.add(1.0);
        floatArray.add(2.0);
        variables.put("var_array_float", floatArray);

        // 对象数组
        List<Map<String, Object>> objectArray = new ArrayList<>();
        Map<String, Object> obj1 = new HashMap<>();
        obj1.put("key", "123");
        Map<String, Object> obj2 = new HashMap<>();
        obj2.put("value", 100);
        objectArray.add(obj1);
        objectArray.add(obj2);
        variables.put("var_array_object", objectArray);

        // Placeholder 变量类型应该是 Message/List<Message>
        String userMessageContent = "Hello!";
        String assistantMessageContent = "Hello!";
        List<Message> placeholder1 = new ArrayList<>();
        placeholder1.add(Message.builder().role(Role.USER).content(userMessageContent).build());
        placeholder1.add(
            Message.builder().role(Role.ASSISTANT).content(assistantMessageContent).build());
        variables.put("placeholder1", placeholder1);

        // 注意：prompt 模板中未提供对应值的变量将被视为空值

        // 4. 格式化 prompt
        List<Message> messages = client.formatPrompt(prompt, variables);

        System.out.println("格式化后的消息:");
        for (Message msg : messages) {
          System.out.println("  Role: " + msg.getRole() + ", Content: " + msg.getContent());
        }

        // 5. 调用 LLM
        callLLM(client, messages);
      }

      System.out.println("\nJinja 模板示例执行成功！");
    } catch (Exception e) {
      System.err.println("执行失败：" + e.getMessage());
      e.printStackTrace();
    } finally {
      // 6. 关闭客户端
      client.close();
    }
  }

  /** 调用 LLM */
  private static void callLLM(CozeLoopClient client, List<Message> messages) {
    try (com.coze.loop.trace.CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
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

      System.out.println("\nLLM 调用完成");
      System.out.println("  输出: " + respChoices[0]);
      System.out.println("  模型: " + modelName);
    }
  }
}
