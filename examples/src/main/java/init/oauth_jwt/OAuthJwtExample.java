package init.oauth_jwt;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * OAuth JWT 认证初始化示例
 *
 * <p>这是生产环境推荐的认证方式，比 PAT 更安全。
 *
 * <p>使用前请先设置以下环境变量： - COZELOOP_WORKSPACE_ID: 你的工作空间 ID - COZELOOP_JWT_OAUTH_CLIENT_ID: 你的客户端 ID -
 * COZELOOP_JWT_OAUTH_PRIVATE_KEY: 你的私钥（PEM 格式） - COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID: 你的公钥 ID
 *
 * <p>创建应用的步骤： 1. 访问 https://www.coze.cn/open/oauth/apps 2. 创建新的应用 3. 妥善保管你的 publicKeyID 和
 * privateKey，防止数据泄露
 */
public class OAuthJwtExample {

  public static void main(String[] args) {
    // 从环境变量获取配置
    String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
    String clientId = System.getenv("COZELOOP_JWT_OAUTH_CLIENT_ID");
    String privateKey = System.getenv("COZELOOP_JWT_OAUTH_PRIVATE_KEY");
    String publicKeyId = System.getenv("COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID");

    if (workspaceId == null || clientId == null || privateKey == null || publicKeyId == null) {
      System.err.println("请设置环境变量：");
      System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
      System.err.println("  COZELOOP_JWT_OAUTH_CLIENT_ID=your_client_id");
      System.err.println("  COZELOOP_JWT_OAUTH_PRIVATE_KEY=your_private_key");
      System.err.println("  COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID=your_public_key_id");
      System.exit(1);
    }

    // 方式1：使用默认配置创建客户端（最简单的方式）
    useDefaultClient();

    // 方式2：使用自定义配置创建客户端
    // useCustomClient(workspaceId, clientId, privateKey, publicKeyId);
  }

  /** 使用默认配置创建客户端 */
  private static void useDefaultClient() {
    // 创建客户端
    // workspaceId and apiToken from env are set to client automatically
    CozeLoopClient client = new CozeLoopClientBuilder().build();

    try {
      // 使用客户端创建 span
      try (CozeLoopSpan span = client.startSpan("first_span", "custom")) {
        span.setTag("example", "oauth_jwt_init");
        System.out.println("使用 OAuth JWT 创建了第一个 span");
      }

      System.out.println("示例执行成功！");
    } finally {
      // force flush trace to backend
      // 警告：一般情况下不需要调用此方法，因为 spans 会自动批量上报。
      // 注意：flush 会阻塞并等待上报完成，可能导致频繁上报，影响性能。
      client.flush();
    }
  }

  /** 使用自定义配置创建客户端 */
  private static void useCustomClient() {
    // 创建带自定义配置的客户端
    CozeLoopClient client =
        new CozeLoopClientBuilder()
            // 可以设置自定义的 base URL（一般不需要）
            // .baseUrl("https://api.coze.cn")
            // 可以设置服务名称
            .serviceName("my-production-service")
            .build();

    try {
      // 使用客户端
      try (CozeLoopSpan span = client.startSpan("custom_span", "custom")) {
        span.setTag("example", "oauth_jwt_custom_config");
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
