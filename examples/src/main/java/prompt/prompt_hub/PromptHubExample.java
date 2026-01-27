package prompt.prompt_hub;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.entity.Message;
import com.coze.loop.entity.Prompt;
import com.coze.loop.entity.Role;
import com.coze.loop.prompt.GetPromptParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt Hub 基础示例
 * 
 * 展示如何：
 * - 获取 prompt
 * - 格式化 prompt（包含普通变量和 placeholder 变量）
 * - 与 LLM 调用的集成
 * 
 * 使用前请先设置以下环境变量：
 * - COZELOOP_WORKSPACE_ID: 你的工作空间 ID
 * - COZELOOP_API_TOKEN: 你的访问令牌
 * 
 * 注意：需要在平台上创建一个 Prompt（Prompt Key 设置为 'prompt_hub_demo'），
 * 并在模板中添加以下消息，然后提交版本：
 * - System: You are a helpful bot, the conversation topic is {{var1}}.
 * - Placeholder: placeholder1
 * - User: My question is {{var2}}
 * - Placeholder: placeholder2
 */
public class PromptHubExample {
    
    public static void main(String[] args) {
        String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
        String apiToken = System.getenv("COZELOOP_API_TOKEN");
        
        if (workspaceId == null || apiToken == null) {
            System.err.println("请设置环境变量：");
            System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
            System.err.println("  COZELOOP_API_TOKEN=your_token");
            System.exit(1);
        }
        
        CozeLoopClient client = new CozeLoopClientBuilder()
            .workspaceId(workspaceId)
            .tokenAuth(apiToken)
            .build();
        
        try {
            // 1. 创建根 span
            try (com.coze.loop.trace.CozeLoopSpan rootSpan = 
                    client.startSpan("root_span", "main_span")) {
                
                // 2. 获取 prompt
                Prompt prompt = client.getPrompt(GetPromptParam.builder()
                    .promptKey("prompt_hub_demo")
                    .version("0.0.1") // 如果不指定版本，将获取对应 prompt 的最新版本
                    .build());
                
                if (prompt == null) {
                    System.err.println("获取 prompt 失败：prompt 不存在");
                    return;
                }
                
                // 3. 获取 prompt 的消息
                if (prompt.getPromptTemplate() != null && 
                    prompt.getPromptTemplate().getMessages() != null) {
                    System.out.println("Prompt 消息:");
                    for (Message msg : prompt.getPromptTemplate().getMessages()) {
                        System.out.println("  Role: " + msg.getRole() + 
                            ", Content: " + msg.getContent());
                    }
                }
                
                // 4. 获取 prompt 的 LLM 配置
                if (prompt.getLlmConfig() != null) {
                    System.out.println("Prompt LLM 配置: " + prompt.getLlmConfig());
                }
                
                // 5. 格式化 prompt 消息
                String userMessageContent = "Hello!";
                String assistantMessageContent = "Hello!";
                
                // 准备变量
                Map<String, Object> variables = new HashMap<>();
                // 普通变量类型应该是 String
                variables.put("var1", "artificial intelligence");
                variables.put("var2", "What is AI?");
                
                // Placeholder 变量类型应该是 Message/List<Message>
                // 注意：prompt 模板中未提供对应值的变量将被视为空值
                List<Message> placeholder1 = new ArrayList<>();
                placeholder1.add(Message.builder()
                    .role(Role.USER)
                    .content(userMessageContent)
                    .build());
                placeholder1.add(Message.builder()
                    .role(Role.ASSISTANT)
                    .content(assistantMessageContent)
                    .build());
                variables.put("placeholder1", placeholder1);
                
                // 格式化 prompt
                List<Message> messages = client.formatPrompt(prompt, variables);
                
                System.out.println("\n格式化后的消息:");
                for (Message msg : messages) {
                    System.out.println("  Role: " + msg.getRole() + 
                        ", Content: " + msg.getContent());
                }
                
                // 6. 调用 LLM
                callLLM(client, messages);
            }
            
            System.out.println("\nPrompt Hub 示例执行成功！");
        } catch (Exception e) {
            System.err.println("执行失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 7. 关闭客户端
            client.close();
        }
    }
    
    /**
     * 调用 LLM
     */
    private static void callLLM(CozeLoopClient client, List<Message> messages) {
        try (com.coze.loop.trace.CozeLoopSpan span = 
                client.startSpan("llmCall", "llm")) {
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
            span.setModel(modelName);
            span.setInputTokens(respPromptTokens);
            span.setOutputTokens(respCompletionTokens);
            
            // 设置首次响应时间
            long firstRespTime = System.currentTimeMillis() * 1000;
            span.setAttribute("start_time_first_resp", firstRespTime);
            
            System.out.println("\nLLM 调用完成");
            System.out.println("  输出: " + respChoices[0]);
            System.out.println("  模型: " + modelName);
        }
    }
}

