# CozeLoop Java SDK
[English](README.md) | [简体中文](README.zh_CN.md)

Java SDK for interacting with [CozeLoop platform](https://loop.coze.cn).

## Features

- **Trace Reporting**: Built on [OpenTelemetry](https://opentelemetry.io/) SDK, automatic batch reporting
  - Two-level batching: OpenTelemetry BatchSpanProcessor + custom 25-span batches
  - Automatic context propagation across threads and async operations
  - Support for Events, Links, and Baggage
  - See [OpenTelemetry Integration Guide](docs/opentelemetry.md) for details
- **Prompt Management**: Pull, cache, and format prompts
- **AOP Annotation**: Declarative tracing with `@CozeTrace` annotation
- **Spring Boot Integration**: Seamless integration with Spring Boot applications

## Requirements

- Java 8+ (Java 11+ recommended)
- Maven 3.6+ or Gradle 6.0+

## Installation

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

## Quick Start

### Initialize

First, visit https://loop.coze.cn/console/enterprise/personal/open/oauth/apps and create an OAuth app.
Then you can get your owner appid, public key and private key.

Set your environment variables:
```bash
export COZELOOP_WORKSPACE_ID=your workspace id
export COZELOOP_JWT_OAUTH_CLIENT_ID=your client id
export COZELOOP_JWT_OAUTH_PRIVATE_KEY=your private key
export COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID=your public key id
```

Or use PAT (Personal Access Token) for testing:
```bash
export COZELOOP_WORKSPACE_ID=your workspace id
export COZELOOP_API_TOKEN=your token
```

### Basic Usage

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

// Initialize client
String workspaceId = System.getenv("COZELOOP_WORKSPACE_ID");
String apiToken = System.getenv("COZELOOP_API_TOKEN");

CozeLoopClient client = new CozeLoopClientBuilder()
    .workspaceId(workspaceId)
    .tokenAuth(apiToken)  // or use .jwtOAuth(clientId, privateKey, publicKeyId)
    .build();

try {
    // Report trace
    try (CozeLoopSpan span = client.startSpan("operation", "custom")) {
        span.setInput("Hello");
        span.setOutput("World");
        span.setAttribute("custom_key", "custom_value");
    }
    
    // Get and format prompt
    Prompt prompt = client.getPrompt(GetPromptParam.builder()
        .promptKey("your_prompt_key")
        .build());
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("var1", "content");
    List<Message> messages = client.formatPrompt(prompt, variables);
} finally {
    // Close client (important: ensures all traces are reported)
    client.close();
}
```

### Report Trace with LLM

```java
try (CozeLoopSpan span = client.startSpan("llmCall", "llm")) {
    String input = "What is the weather in Shanghai?";
    
    // Call your LLM API here
    String output = callLLMAPI(input);
    
    // Set span attributes
    span.setInput(input);
    span.setOutput(output);
    span.setModelProvider("openai");
    span.setModel("gpt-4o-2024-05-13");
    span.setInputTokens(11);
    span.setOutputTokens(52);
}
```

### Spring Boot Integration

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

**Use Annotations**

```java
import com.coze.loop.spring.annotation.CozeTrace;
import org.springframework.stereotype.Service;

@Service
public class LLMService {
    
    @CozeTrace(value = "llm_call", spanType = "llm", captureArgs = true, captureReturn = true)
    public String callLLM(String prompt) {
        return llmClient.call(prompt);
    }
}
```

## Documentation

- [Examples](examples/README.md) - Code examples demonstrating SDK usage
- [OpenTelemetry Integration Guide](docs/opentelemetry.md) - Detailed guide on OpenTelemetry integration
  - Architecture overview
  - Two-level batching strategy
  - Context propagation
  - Advanced features (Events, Links, Baggage)
  - Best practices
- [API Documentation](docs/api.md) - API reference
- [Configuration](docs/configuration.md) - Configuration options

### OpenTelemetry Benefits

The SDK is built on OpenTelemetry, providing:

- **Industry Standard**: Widely adopted observability framework
- **Vendor Neutral**: Works with any backend that supports OpenTelemetry
- **Rich Ecosystem**: Extensive instrumentation libraries
- **Automatic Batching**: Built-in batch processing for efficient export
- **Context Propagation**: Automatic trace context propagation across services
- **Mature & Battle-Tested**: Production-ready with excellent performance

For more details, see the [OpenTelemetry Integration Guide](docs/opentelemetry.md).

## Building from Source

```bash
git clone https://github.com/coze-dev/cozeloop-java.git
cd cozeloop-java
mvn clean install
```

## Contribution

Please check [Contributing](CONTRIBUTING.md) for more details.

## Security

If you discover a potential security issue in this project, or think you may
have discovered a security issue, we ask that you notify Bytedance Security via our [security center](https://security.bytedance.com/src) or [vulnerability reporting email](sec@bytedance.com).

Please do **not** create a public GitHub issue.

## License

This project is licensed under the [MIT License](LICENSE).

