# 测试策略与运行指南

## 测试栈
- 单元测试：JUnit 5（`org.junit.jupiter:junit-jupiter`）。
- 断言：AssertJ（`org.assertj:assertj-core`）。
- Mock：Mockito（`org.mockito:mockito-core`、`mockito-junit-jupiter`）。
- Spring 测试：`spring-boot-starter-test` 覆盖 `Starter` 模块的自动配置与 AOP。
- HTTP 测试：OkHttp `MockWebServer` 用于服务端模拟。

## 目录结构
- 核心模块：`cozeloop-core/src/test/java` 按功能包组织（client、http、prompt、internal、auth 等）。
- Starter 模块：`cozeloop-spring-boot-starter/src/test/java` 覆盖自动配置、属性绑定、AOP 与集成路径。

## 运行方式
- 全量：在项目根执行 `mvn -q test`。
- 指定模块：`mvn -q -pl cozeloop-core -am test` 或 `-pl cozeloop-spring-boot-starter -am test`。

## 范围与覆盖
- 客户端生命周期与配置装配。
- HTTP 拦截器与重试、日志、鉴权。
- Prompt 渲染、缓存、校验与模板引擎实现。
- Trace 生成、转换与导出逻辑。
- 流式解析：事件解码与结果提取（新增示例见 `stream` 相关测试）。
- Starter 自动配置、AOP 与跨模块集成测试。

## 编写规范
- 命名：`ClassNameTest`，同包位于 `src/test/java`。
- 结构：Given/When/Then，断言使用 AssertJ，Mock 使用 Mockito。
- 隔离：外部交互通过 Mock 或 `MockWebServer`，避免网络依赖。
- 可读性：一测一责，避免过度耦合；必要时提取测试工具类。

## 示例与最佳实践
- HTTP 客户端：使用 `MockWebServer` 验证拦截器与重试策略。
- OpenTelemetry：`OpenTelemetryTestUtils` 提供采集与断言工具（Starter 模块）。
- 流式处理：构造 SSE 文本流验证 `SSEDecoder` 与 `StreamReader` 的事件切分、错误跳过与关闭语义。