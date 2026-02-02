package trace.parent_child;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.trace.CozeLoopSpan;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 父子 Span 示例
 * 
 * 展示如何创建父子关系的 span，包括：
 * - 创建父子关系的 span
 * - 展示 context 传递机制
 * - 包含异步任务的追踪示例
 * 
 * 使用前请先设置以下环境变量：
 * - COZELOOP_WORKSPACE_ID: 你的工作空间 ID
 * - COZELOOP_API_TOKEN: 你的访问令牌
 */
public class ParentChildSpanExample {
    
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
            // 1. 创建根 span（因为没有父 span，所以这是新 trace 的根 span）
            try (CozeLoopSpan rootSpan = client.startSpan("root_span", "main_span")) {
                // 2. 设置自定义标签
                rootSpan.setTag("service_name", "core");
                
                // 3. 设置自定义属性（这些属性会传递给子 span）
                rootSpan.setTag("product_id", "123456654321");
                rootSpan.setTag("product_name", "AI bot");
                rootSpan.setTag("product_version", "0.0.1");
                
                // 4. 设置用户 ID（会隐式设置 tag key: user_id）
                rootSpan.setTag("user_id", "123456");
                
                // 5. 调用 LLM（这会创建一个子 span）
                boolean success = callLLM(client);
                
                if (!success) {
                    rootSpan.setStatusCode(ERROR_CODE_LLM_CALL);
                    rootSpan.setError(new RuntimeException("LLM 调用失败"));
                }
                
                // 6. 假设需要运行一个异步任务，它的 span 是 rootSpan 的子 span
                CompletableFuture<Void> asyncTask = asyncRendering(client, rootSpan);
                
                // 等待异步任务完成（在实际服务中，这个延迟不是必需的）
                try {
                    asyncTask.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.err.println("异步任务执行异常：" + e.getMessage());
                }
            }
            
            // 由于 asyncRending 在单独的线程中运行，它的 finish 方法可能会稍后执行。
            // 这里我们故意添加一个延迟来模拟服务的持续运行。
            // 在实际服务中，这个延迟不是必需的。
            Thread.sleep(2000);
            
            System.out.println("父子 Span 示例执行成功！");
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
    
    /**
     * LLM 调用（作为 rootSpan 的子 span）
     */
    private static boolean callLLM(CozeLoopClient client) {
        // llmCall span 通过 context 自动成为 rootSpan 的子 span
        try (CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
            // 模拟 LLM 处理
            String modelName = "gpt-4o-2024-05-13";
            String input = "上海天气怎么样？";
            
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
            
            // 设置 span 属性
            span.setInput(input);
            span.setOutput(respChoices);
            span.setModelProvider("openai");
            span.setModelName(modelName);
            span.setInputTokens(respPromptTokens);
            span.setOutputTokens(respCompletionTokens);
            
            System.out.println("LLM 调用完成（子 span）");
            return true;
        }
    }
    
    /**
     * 异步渲染任务（作为 rootSpan 的子 span）
     * 
     * 注意：在实际应用中，OpenTelemetry 的 context 传播是自动的。
     * 在同一个线程中，在父 span 的 scope 内创建的子 span 会自动成为父子关系。
     * 但在异步任务中，需要手动传递 context。
     */
    private static CompletableFuture<Void> asyncRendering(CozeLoopClient client, CozeLoopSpan parentSpan) {
        // 获取当前 context（包含父 span 信息）

        return CompletableFuture.runAsync(() -> {
            // 在新的线程中设置 context
            try (CozeLoopSpan asyncSpan = client.startSpan("asyncRendering", "rendering", parentSpan)) {
                // 模拟异步处理
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // 设置状态码
                asyncSpan.setStatusCode(0);

                System.out.println("异步渲染任务完成（子 span）");
            }
        });
    }
}

