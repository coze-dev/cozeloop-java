# 项目架构总览

## 模块结构
- `cozeloop-core`：SDK 核心能力，包含客户端、认证、HTTP、Prompt、Trace、流式处理、实体与工具等。
- `cozeloop-spring-boot-starter`：Spring Boot 自动配置与 AOP 插桩，便于落地集成。
- `examples`：示例工程，展示认证、Prompt 使用与 Trace 上报等场景。

## 核心分层
- 客户端：`CozeLoopClient`、`CozeLoopClientImpl`、`CozeLoopClientBuilder`（`cozeloop-core/src/main/java/com/coze/loop/client`）。
- 配置：`CozeLoopConfig`（`cozeloop-core/src/main/java/com/coze/loop/config/CozeLoopConfig.java`）。
- 认证：`Auth`、`TokenAuth`、`JWTOAuthAuth`（`cozeloop-core/src/main/java/com/coze/loop/auth`）。
- HTTP：`HttpClient` 与拦截器 `AuthInterceptor`、`RetryInterceptor`、`LoggingInterceptor`（`cozeloop-core/src/main/java/com/coze/loop/http`）。
- Prompt：`PromptProvider`、`PromptCache`、`TemplateEngine` 及实现 `NormalTemplateEngine`、`Jinja2TemplateEngine`、`PromptFormatter`、`VariableValidator`（`cozeloop-core/src/main/java/com/coze/loop/prompt`）。
- Trace：`CozeLoopTracerProvider`、`CozeLoopSpan`、`CozeLoopSpanExporter`、`SpanConverter`、`FileUploader`（`cozeloop-core/src/main/java/com/coze/loop/trace`）。
- 流式处理：`SSEDecoder`、`StreamReader`、`SSEParser`、`ServerSentEvent`（`cozeloop-core/src/main/java/com/coze/loop/stream`）。
- 实体模型：消息与执行参数、模板、工具调用等 VO（`cozeloop-core/src/main/java/com/coze/loop/entity`）。
- 工具：`JsonUtils`、`ValidationUtils`、`IdGenerator`（`cozeloop-core/src/main/java/com/coze/loop/internal`）。
- 异常：`CozeLoopException` 及细分异常与错误码（`cozeloop-core/src/main/java/com/coze/loop/exception`）。

## Spring Boot Starter
- 自动配置：`CozeLoopAutoConfiguration`（`cozeloop-spring-boot-starter/src/main/java/com/coze/loop/spring/autoconfigure/CozeLoopAutoConfiguration.java`）。
- 配置属性：`CozeLoopProperties`（`cozeloop-spring-boot-starter/src/main/java/com/coze/loop/spring/config/CozeLoopProperties.java`）。
- AOP 插桩：注解 `CozeTrace` 与切面 `CozeTraceAspect`（`cozeloop-spring-boot-starter/src/main/java/com/coze/loop/spring/aop`）。
- 自动导入：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 与 `spring.factories`。

## 典型流程
- 初始化客户端并注入认证拦截器，经 `HttpClient` 调用后端。
- 从 Prompt 源获取模板，`PromptFormatter` 渲染并校验变量。
- 通过 `CozeLoopTracerProvider` 创建 Span，经 `CozeLoopSpanExporter` 上报并可携带附件（`FileUploader`）。
- 对于流式输出，`SSEDecoder` 解析字节流为事件，`StreamReader` 驱动 `SSEParser` 产出类型化结果。

## 扩展点
- 模板引擎：实现 `TemplateEngine` 扩展渲染能力。
- 流式解析：实现 `SSEParser<T>` 定义事件到领域对象的映射与错误处理。
- 认证策略：实现 `Auth` 扩充鉴权方式。