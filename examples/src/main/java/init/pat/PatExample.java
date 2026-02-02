package init.pat;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * Personal Access Token (PAT) 初始化示例
 * 
 * 重要提示：Personal Access Token 不够安全，仅用于测试环境。
 * 生产环境请使用 OAuth JWT 认证方式。
 * 
 * 使用前请先设置以下环境变量：
 * - COZELOOP_WORKSPACE_ID: 你的工作空间 ID
 * - COZELOOP_API_TOKEN: 你的访问令牌
 * 
 * 创建令牌的步骤：
 * 1. 访问 https://www.coze.cn/open/oauth/pat
 * 2. 创建新的令牌
 * 3. 妥善保管你的令牌，防止数据泄露
 */
public class PatExample {
    
    public static void main(String[] args) {
        // 从环境变量获取配置
        String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
        String apiToken = System.getenv("COZELOOP_API_TOKEN");
        
        if (workspaceId == null || apiToken == null) {
            System.err.println("请设置环境变量：");
            System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
            System.err.println("  COZELOOP_API_TOKEN=your_token");
            System.exit(1);
        }
        
        // 方式1：使用默认配置创建客户端（最简单的方式）
        useDefaultClient(workspaceId, apiToken);
        
        // 方式2：使用自定义配置创建客户端
        // useCustomClient(workspaceId, apiToken);
    }
    
    /**
     * 使用默认配置创建客户端
     */
    private static void useDefaultClient(String workspaceId, String apiToken) {
        // 创建客户端
        CozeLoopClient client = new CozeLoopClientBuilder()
            .workspaceId(workspaceId)
            .tokenAuth(apiToken)
            .build();
        
        try {
            // 使用客户端创建 span
            try (CozeLoopSpan span = client.startSpan("first_span", "custom")) {
                span.setTag("example", "pat_init");
                System.out.println("使用 PAT 创建了第一个 span");
            }
            
            System.out.println("示例执行成功！");
        } finally {
            // force flush trace to backend
            // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
            // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
            client.flush();
        }
    }
    
    /**
     * 使用自定义配置创建客户端
     */
    private static void useCustomClient(String workspaceId, String apiToken) {
        // 创建带自定义配置的客户端
        CozeLoopClient client = new CozeLoopClientBuilder()
            .workspaceId(workspaceId)
            .tokenAuth(apiToken)
            // 可以设置自定义的 base URL（一般不需要）
            // .baseUrl("https://api.coze.cn")
            // 可以设置服务名称
            .serviceName("my-custom-service")
            .build();
        
        try {
            // 使用客户端
            try (CozeLoopSpan span = client.startSpan("custom_span", "custom")) {
                span.setTag("example", "pat_custom_config");
                System.out.println("使用自定义配置创建了 span");
            }
            
            System.out.println("自定义配置示例执行成功！");
        } finally {
            // force flush trace to backend
            // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
            // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
            client.flush();
        }
    }
}

