package init.error;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.trace.CozeLoopSpan;

/**
 * 错误处理示例
 *
 * <p>展示如何正确处理 CozeLoop SDK 中的异常和错误。
 *
 * <p>使用前请先设置以下环境变量： - COZELOOP_WORKSPACE_ID: 你的工作空间 ID - COZELOOP_API_TOKEN: 你的访问令牌（或使用 OAuth JWT）
 */
public class ErrorHandlingExample {

  public static void main(String[] args) {
    String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
    String apiToken = System.getenv("COZELOOP_API_TOKEN");

    if (workspaceId == null || apiToken == null) {
      System.err.println("请设置环境变量：");
      System.err.println("  COZELOOP_WORKSPACE_ID=your_workspace_id");
      System.err.println("  COZELOOP_API_TOKEN=your_token");
      System.exit(1);
    }

    // 示例1：客户端初始化错误处理
    handleClientInitializationError(workspaceId, apiToken);

    // 示例2：Span 操作错误处理
    handleSpanOperationError(workspaceId, apiToken);

    // 示例3：业务逻辑错误处理
    handleBusinessLogicError(workspaceId, apiToken);
  }

  /** 示例1：客户端初始化错误处理 */
  private static void handleClientInitializationError(String workspaceId, String apiToken) {
    try {
      // 如果配置错误，build() 会抛出 CozeLoopException
      CozeLoopClient client =
          new CozeLoopClientBuilder().workspaceId(workspaceId).tokenAuth(apiToken).build();

      System.out.println("客户端初始化成功");
      client.close();
    } catch (CozeLoopException e) {
      System.err.println("客户端初始化失败：" + e.getMessage());
      System.err.println("错误代码：" + e.getErrorCode());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("未知错误：" + e.getMessage());
      e.printStackTrace();
    }
  }

  /** 示例2：Span 操作错误处理 */
  private static void handleSpanOperationError(String workspaceId, String apiToken) {
    CozeLoopClient client = null;
    try {
      client = new CozeLoopClientBuilder().workspaceId(workspaceId).tokenAuth(apiToken).build();

      // 正常使用 span
      try (CozeLoopSpan span = client.startSpan("test_span", "custom")) {
        span.setInput("test input");
        span.setOutput("test output");
        // 如果操作失败，可以设置错误状态
        // span.setError(new RuntimeException("业务错误"));
      }

      System.out.println("Span 操作成功");
    } catch (CozeLoopException e) {
      System.err.println("CozeLoop 操作失败：" + e.getMessage());
      System.err.println("错误代码：" + e.getErrorCode());
    } catch (Exception e) {
      System.err.println("未知错误：" + e.getMessage());
      e.printStackTrace();
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {
          System.err.println("关闭客户端时出错：" + e.getMessage());
        }
      }
    }
  }

  /** 示例3：业务逻辑错误处理 */
  private static void handleBusinessLogicError(String workspaceId, String apiToken) {
    CozeLoopClient client = null;
    try {
      client = new CozeLoopClientBuilder().workspaceId(workspaceId).tokenAuth(apiToken).build();

      // 模拟业务逻辑
      try (CozeLoopSpan span = client.startSpan("business_operation", "custom")) {
        span.setInput("开始业务操作");

        // 模拟可能失败的业务逻辑
        boolean success = performBusinessLogic();

        if (success) {
          span.setOutput("业务操作成功");
          span.setStatusCode(0); // 0 表示成功
          System.out.println("业务操作成功");
        } else {
          // 业务失败时，设置错误信息
          RuntimeException error = new RuntimeException("业务逻辑执行失败");
          span.setError(error);
          span.setStatusCode(1); // 非0 表示失败
          System.err.println("业务操作失败");
        }
      }
    } catch (CozeLoopException e) {
      System.err.println("CozeLoop SDK 错误：" + e.getMessage());
      System.err.println("错误代码：" + e.getErrorCode());
    } catch (Exception e) {
      System.err.println("业务异常：" + e.getMessage());
      e.printStackTrace();
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {
          System.err.println("关闭客户端时出错：" + e.getMessage());
        }
      }
    }
  }

  /** 模拟业务逻辑（可能成功或失败） */
  private static boolean performBusinessLogic() {
    // 模拟业务逻辑，这里随机返回成功或失败
    return Math.random() > 0.3; // 70% 成功率
  }
}
