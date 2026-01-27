package trace.simple;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * 简单追踪示例
 * 
 * 展示基本的 span 创建和使用，包括：
 * - 创建 span
 * - 设置 input/output
 * - 设置 model 信息
 * - 设置 tokens 信息
 * - 展示 LLM 调用的完整追踪流程
 * 
 * 使用前请先设置以下环境变量：
 * - COZELOOP_WORKSPACE_ID: 你的工作空间 ID
 * - COZELOOP_API_TOKEN: 你的访问令牌
 */
public class SimpleTraceExample {
    
    private static final int ERROR_CODE_LLM_CALL = 600789111;
    
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
            try (CozeLoopSpan rootSpan = client.startSpan("root_span", "main_span")) {
                // 2. 设置自定义标签
                rootSpan.setAttribute("mode", "simple");
                rootSpan.setAttribute("node_id", 6076665L);
                rootSpan.setAttribute("node_process_duration", 228.6);
                rootSpan.setAttribute("is_first_node", true);
                
                // 3. 调用 LLM
                boolean success = callLLM(client);
                
                if (!success) {
                    // 4. 如果失败，设置错误状态码和错误信息
                    rootSpan.setStatusCode(ERROR_CODE_LLM_CALL);
                    rootSpan.setError(new RuntimeException("LLM 调用失败"));
                } else {
                    rootSpan.setStatusCode(0); // 0 表示成功
                }
            }
            
            // 5. （可选）强制刷新，上报所有 traces
            // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
            // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
            // client.flush(); // 注意：CozeLoopClient 可能没有 flush 方法
            
            System.out.println("追踪示例执行成功！");
        } catch (Exception e) {
            System.err.println("执行失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. 关闭客户端，执行 flush 并关闭连接
            // 警告：一旦执行 Close，客户端将不可用，需要通过 NewClient 创建新客户端！
            // 仅在需要释放资源时使用，例如关闭实例时！
            client.close();
        }
    }
    
    /**
     * 模拟 LLM 调用
     */
    private static boolean callLLM(CozeLoopClient client) {
        // 创建 LLM 调用的 span
        try (CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
            // 模拟 LLM 处理
            String modelName = "gpt-4o-2024-05-13";
            String input = "上海天气怎么样？";
            
            // 模拟 API 调用（实际使用时替换为真实的 LLM API 调用）
            // 这里只是模拟
            try {
                Thread.sleep(1000); // 模拟网络延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            
            // 模拟响应
            String[] respChoices = {"上海天气晴朗，气温25摄氏度。"};
            int respPromptTokens = 11;
            int respCompletionTokens = 52;
            
            // 设置 span 的输入
            span.setInput(input);
            
            // 设置 span 的输出
            span.setOutput(respChoices);
            
            // 设置 model provider，例如：openai, anthropic 等
            span.setModelProvider("openai");
            
            // 设置 model name，例如：gpt-4-1106-preview 等
            span.setModel(modelName);
            
            // 设置输入 tokens 数量
            // 当设置了 input_tokens 和 output_tokens 后，会自动计算 total_tokens
            span.setInputTokens(respPromptTokens);
            
            // 设置输出 tokens 数量
            span.setOutputTokens(respCompletionTokens);
            
            // 设置首次响应时间（微秒）
            // 当设置了 start_time_first_resp 后，会根据 span 的 StartTime 计算
            // 一个名为 latency_first_resp 的标签，表示首次数据包的延迟
            long firstRespTime = System.currentTimeMillis() * 1000; // 转换为微秒
            span.setAttribute("start_time_first_resp", firstRespTime);
            
            System.out.println("LLM 调用成功");
            System.out.println("  输入: " + input);
            System.out.println("  输出: " + respChoices[0]);
            System.out.println("  模型: " + modelName);
            System.out.println("  输入 tokens: " + respPromptTokens);
            System.out.println("  输出 tokens: " + respCompletionTokens);
            
            return true;
        }
    }
}

