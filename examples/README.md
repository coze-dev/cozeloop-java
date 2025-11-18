# CozeLoop Java SDK 示例代码

本目录包含了 CozeLoop Java SDK 的完整示例代码，帮助开发者快速上手使用 SDK。

## 目录结构

```
examples/
├── init/                      # 初始化示例
│   ├── pat/                   # Personal Access Token 初始化
│   ├── oauth_jwt/             # OAuth JWT 认证初始化
│   └── error/                 # 错误处理示例
├── trace/                     # 追踪示例
│   ├── simple/                # 简单追踪示例
│   ├── parent_child/          # 父子 span 示例
│   └── prompt/                # 追踪与提示结合示例
└── prompt/                    # 提示管理示例
    ├── prompt_hub/            # Prompt Hub 基础示例
    └── prompt_hub_jinja/      # Jinja 模板示例
```

## 环境变量配置

在运行示例之前，请先设置以下环境变量：

### 使用 PAT (Personal Access Token) 认证

```bash
export COZELOOP_WORKSPACE_ID=your_workspace_id
export COZELOOP_API_TOKEN=your_token
```

**注意**：PAT 仅用于测试环境，生产环境请使用 OAuth JWT 认证。

### 使用 OAuth JWT 认证（推荐）

```bash
export COZELOOP_WORKSPACE_ID=your_workspace_id
export COZELOOP_JWT_OAUTH_CLIENT_ID=your_client_id
export COZELOOP_JWT_OAUTH_PRIVATE_KEY=your_private_key
export COZELOOP_JWT_OAUTH_PUBLIC_KEY_ID=your_public_key_id
```

## 快速开始

### 1. 构建项目

首先需要构建父项目：

```bash
cd /Users/jiafan/Desktop/poc/cozeloop-java
mvn clean install
```

### 2. 运行示例

#### 方式一：使用 Maven Exec 插件

```bash
cd examples
mvn exec:java -Dexec.mainClass="init.pat.PatExample"
```

#### 方式二：直接运行 Java 类

```bash
cd examples
# 编译
mvn compile

# 运行（需要设置 classpath）
java -cp target/classes:../cozeloop-core/target/cozeloop-core-1.0.0-SNAPSHOT.jar:... init.pat.PatExample
```

## 示例说明

### 初始化示例 (init/)

#### PAT 初始化 (`init/pat/PatExample.java`)

展示如何使用 Personal Access Token 初始化客户端。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="init.pat.PatExample"
```

**要点：**
- PAT 仅用于测试环境
- 生产环境应使用 OAuth JWT
- 展示基本用法和自定义配置用法

#### OAuth JWT 初始化 (`init/oauth_jwt/OAuthJwtExample.java`)

展示如何使用 OAuth JWT 认证初始化客户端（生产环境推荐）。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="init.oauth_jwt.OAuthJwtExample"
```

**要点：**
- 生产环境推荐的认证方式
- 支持环境变量和代码配置两种方式

#### 错误处理 (`init/error/ErrorHandlingExample.java`)

展示如何正确处理异常和错误。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="init.error.ErrorHandlingExample"
```

**要点：**
- 客户端初始化错误处理
- Span 操作错误处理
- 业务逻辑错误处理

### 追踪示例 (trace/)

#### 简单追踪 (`trace/simple/SimpleTraceExample.java`)

展示基本的 span 创建和使用。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="trace.simple.SimpleTraceExample"
```

**要点：**
- 创建 span
- 设置 input/output
- 设置 model 信息
- 设置 tokens 信息
- 完整的 LLM 调用追踪流程

#### 父子 Span (`trace/parent_child/ParentChildSpanExample.java`)

展示如何创建父子关系的 span。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="trace.parent_child.ParentChildSpanExample"
```

**要点：**
- 创建父子关系的 span
- Context 传递机制
- 异步任务的追踪

#### 追踪与提示结合 (`trace/prompt/TraceWithPromptExample.java`)

展示在追踪过程中使用 prompt。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="trace.prompt.TraceWithPromptExample"
```

**要点：**
- 在追踪过程中获取 prompt
- 格式化 prompt
- 与 LLM 调用的完整流程

**前置条件：**
- 需要在平台上创建一个 Prompt（Prompt Key: `prompt_hub_demo`）

### 提示管理示例 (prompt/)

#### Prompt Hub 基础 (`prompt/prompt_hub/PromptHubExample.java`)

展示如何获取和格式化 prompt。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="prompt.prompt_hub.PromptHubExample"
```

**要点：**
- 获取 prompt
- 格式化 prompt（普通变量和 placeholder 变量）
- 与 LLM 调用的集成

**前置条件：**
- 需要在平台上创建一个 Prompt（Prompt Key: `prompt_hub_demo`）
- Prompt 模板应包含：
  - System: You are a helpful bot, the conversation topic is {{var1}}.
  - Placeholder: placeholder1
  - User: My question is {{var2}}.
  - Placeholder: placeholder2

#### Jinja 模板 (`prompt/prompt_hub_jinja/PromptHubJinjaExample.java`)

展示 Jinja 模板的使用和各种变量类型。

**运行方式：**
```bash
mvn exec:java -Dexec.mainClass="prompt.prompt_hub_jinja.PromptHubJinjaExample"
```

**要点：**
- Jinja 模板的使用
- 各种变量类型的格式化（string, int, bool, float, object, array）
- 复杂数据结构的处理

**前置条件：**
- 需要在平台上创建一个 Prompt（Prompt Key: `prompt_hub_demo`）

## 创建 Prompt

在运行 prompt 相关示例之前，需要在 CozeLoop 平台上创建相应的 Prompt：

1. 访问 CozeLoop 平台的 Prompt 开发页面
2. 创建新的 Prompt，设置 Prompt Key 为 `prompt_hub_demo`
3. 在模板中添加消息（参考各示例的注释说明）
4. 提交版本

## 常见问题

### 1. 如何获取 Workspace ID 和 Token？

- **Workspace ID**: 在 CozeLoop 平台的工作空间设置中查看
- **PAT Token**: 访问 https://www.coze.cn/open/oauth/pat 创建
- **OAuth JWT**: 访问 https://www.coze.cn/open/oauth/apps 创建应用

### 2. 示例运行失败怎么办？

- 检查环境变量是否正确设置
- 检查网络连接是否正常
- 检查 Workspace ID 和 Token 是否正确
- 查看错误日志获取详细信息

### 3. 如何自定义配置？

参考各示例中的 `useCustomClient()` 方法，展示如何设置自定义配置。

### 4. 为什么需要关闭客户端？

客户端关闭时会自动刷新并上报所有待上报的 traces。如果不关闭客户端，可能会丢失未上报的 traces。

## 更多资源

- [CozeLoop 官方文档](https://loop.coze.cn/open/docs)
- [Java SDK API 文档](../README.md)
- [Python SDK 示例](https://loop.coze.cn/open/docs/cozeloop/python-sdk)

## 许可证

本示例代码遵循与主项目相同的 MIT 许可证。

