# CozeLoop Java SDK
[English](README.md) | 简体中文

## 概述

CozeLoop Java SDK 是一个用于与 [扣子罗盘平台](https://loop.coze.cn) 进行交互的 Java 客户端。

主要功能：
- **Trace 上报**：基于 [OpenTelemetry](https://opentelemetry.io/) SDK，自动批量上报
  - 两级批处理：OpenTelemetry BatchSpanProcessor + 自定义 25 个 span 批次
  - 跨线程和异步操作的自动上下文传播
  - 支持 Events、Links 和 Baggage
  - 详见 [OpenTelemetry 集成指南](docs/opentelemetry.md)
- **Prompt 管理**：拉取、缓存、格式化、执行 prompts
- **AOP 注解**：使用 `@CozeTrace` 注解进行声明式追踪
- **Spring Boot 集成**：与 Spring Boot 应用无缝集成

## 要求

- Java 8+ (推荐 Java 11+)
- Maven 3.6+

## 安装

### Maven

```xml
<dependency>
    <groupId>com.coze</groupId>
    <artifactId>cozeloop-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Spring Boot Starter

```xml
<dependency>
    <groupId>com.coze</groupId>
    <artifactId>cozeloop-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 快速开始

### 初始化

首先，访问 https://loop.coze.cn/console/enterprise/personal/open/oauth/apps 并创建一个 OAuth 应用，
获取应用所有者的 AppID、公钥和私钥。

设置环境变量：
```bash
export COZELOOP_WORKSPACE_ID=your workspace id
export COZELOOP_JWT_OAUTH_CLIENT_ID=your client id
export COZELOOP_JWT_OAUTH_PRIVATE_KEY=your private key
export COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID=your public key id
```

或使用 PAT (Personal Access Token) 进行测试：
```bash
export COZELOOP_WORKSPACE_ID=your workspace id
export COZELOOP_API_TOKEN=your token
```

### 基本用法

```java
import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.trace.CozeLoopSpan;
import com.coze.loop.entity.Prompt;
import com.coze.loop.entity.Message;
import com.coze.loop.prompt.GetPromptParam;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

// 初始化客户端
String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
String apiToken = System.getenv("COZELOOP_API_TOKEN");

CozeLoopClient client = new CozeLoopClientBuilder()
    .workspaceId(workspaceId)
    .tokenAuth(apiToken)  // 或使用 .jwtOAuth(clientId, privateKey, publicKeyId)
    .build();

try {
    // 上报 trace
    try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
        span.setInput("Hello");
        span.setOutput("World");
        span.setAttribute("custom_key", "custom_value");
    }
    
    // 获取和格式化 prompt
    Prompt prompt = client.getPrompt(GetPromptParam.builder()
        .promptKey("your_prompt_key")
        .build());
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("var1", "content");
    List<Message> messages = client.formatPrompt(prompt, variables);
} finally {
    // 关闭客户端（重要：确保所有 traces 都被上报）
    client.close();
}
```

### Trace 上报与 LLM 调用

```java
try (CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
    String input = "上海天气怎么样？";
    
    // 调用你的 LLM API
    String output = callLLMAPI(input);
    
    // 设置 span 属性
    span.setInput(input);
    span.setOutput(output);
    span.setModelProvider("openai");
    span.setModel("gpt-4o-2024-05-13");
    span.setInputTokens(11);
    span.setOutputTokens(52);
}
```

### Spring Boot 集成

**application.yml**

```yaml
cozeloop:
  workspace-id: your_workspace_id
  jwt:
    client-id: your_client_id
    private-key: your_private_key
    public-key-id: your_public_key_id
  trace:
    enabled: true
```

**使用注解**

```java
@Service
public class LLMService {
    
    @CozeTrace(value = "llm_call", spanType = "llm", captureArgs = true, captureReturn = true)
    public String callLLM(String prompt) {
        return llmClient.call(prompt);
    }
}
```

你可以在 [这里](examples) 查看更多示例。

## 文档

- [示例](examples/README.md) - 演示 SDK 用法的代码示例
- [OpenTelemetry 集成指南](docs/opentelemetry.md) - OpenTelemetry 集成的详细指南
  - 架构概述
  - 两级批处理策略
  - 上下文传播
  - 高级功能（Events、Links、Baggage）
  - 最佳实践
- [API 文档](docs/api.md) - API 参考
- [配置](docs/configuration.md) - 配置选项

### OpenTelemetry 优势

SDK 基于 OpenTelemetry 构建，提供：

- **行业标准**：广泛采用的观测性框架
- **厂商中立**：可与任何支持 OpenTelemetry 的后端配合使用
- **丰富的生态系统**：广泛的工具库
- **自动批处理**：内置批处理机制，高效导出
- **上下文传播**：跨服务的自动追踪上下文传播
- **成熟且经过验证**：生产就绪，性能优异

更多详情，请参阅 [OpenTelemetry 集成指南](docs/opentelemetry.md)。

## 从源码构建

```bash
git clone https://github.com/coze-dev/cozeloop-java.git
cd cozeloop-java
mvn clean install
```

## 贡献

如需了解更多详细信息，请查看 [Contributing](CONTRIBUTING.md)。

## 安全

如果你发现本项目中存在潜在的安全问题，或者认为自己可能发现了安全问题，请通过我们的 [安全中心](https://security.bytedance.com/src) 或 [漏洞报告邮箱](sec@bytedance.com) 通知字节跳动安全团队。
请**不要**创建公开的 GitHub 问题。

## License

本项目采用 [MIT License](LICENSE)。

