# Spring Boot 3.x 支持说明

本文档说明 CozeLoop Spring Boot Starter 对 Spring Boot 3.x 的支持情况。

## 支持的 Spring Boot 版本

- **Spring Boot 2.7.x**: 完全支持（使用 `spring.factories`）
- **Spring Boot 3.x**: 完全支持（使用 `AutoConfiguration.imports`）

## Spring Boot 3.x 的主要变化

根据 [Spring Boot 3.x 自定义 Starter 指南](https://blog.csdn.net/a1256afafaafr/article/details/147768713)，主要变化包括：

### 1. 自动配置注册方式

**Spring Boot 2.x** (旧方式):
```
META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.coze.loop.spring.autoconfigure.CozeLoopAutoConfiguration
```

**Spring Boot 3.x** (新方式):
```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.coze.loop.spring.autoconfigure.CozeLoopAutoConfiguration
```

### 2. 配置类优化

Spring Boot 3.x 推荐使用 `@Configuration(proxyBeanMethods = false)` 来优化性能：

```java
@Configuration(proxyBeanMethods = false)
public class CozeLoopAutoConfiguration {
    // ...
}
```

### 3. Java 版本要求

- **Spring Boot 2.x**: 需要 Java 8+
- **Spring Boot 3.x**: 需要 Java 17+

## 实现细节

### 文件结构

```
cozeloop-spring-boot-starter/
└── src/
    └── main/
        └── resources/
            └── META-INF/
                ├── spring.factories                    # Spring Boot 2.x
                └── spring/
                    └── org.springframework.boot.autoconfigure.AutoConfiguration.imports  # Spring Boot 3.x
```

### 兼容性策略

为了同时支持 Spring Boot 2.x 和 3.x，我们采用了以下策略：

1. **保留 `spring.factories`**: 确保 Spring Boot 2.x 可以正常工作
2. **添加 `AutoConfiguration.imports`**: 确保 Spring Boot 3.x 可以正常工作
3. **配置类优化**: 使用 `proxyBeanMethods = false` 以兼容 Spring Boot 3.x 的最佳实践

### 使用方式

#### Spring Boot 2.x 项目

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.x</version>
</parent>

<dependency>
    <groupId>com.coze.loop</groupId>
    <artifactId>cozeloop-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### Spring Boot 3.x 项目

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.x.x</version>
</parent>

<dependency>
    <groupId>com.coze.loop</groupId>
    <artifactId>cozeloop-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

配置方式完全相同：

```properties
cozeloop.workspace-id=your-workspace-id
cozeloop.auth.token=your-token
```

## 测试

### 测试 Spring Boot 2.x 兼容性

```bash
# 使用 Spring Boot 2.7.x 测试
mvn test -Dspring-boot.version=2.7.18
```

### 测试 Spring Boot 3.x 兼容性

```bash
# 使用 Spring Boot 3.x 测试
mvn test -Dspring-boot.version=3.2.0
```

## 注意事项

1. **Java 版本**: 
   - Spring Boot 2.x 项目可以使用 Java 8+
   - Spring Boot 3.x 项目必须使用 Java 17+

2. **依赖兼容性**: 
   - 确保所有依赖都与目标 Spring Boot 版本兼容

3. **配置属性**: 
   - 配置属性名称和结构在两个版本中保持一致

4. **API 兼容性**: 
   - CozeLoop SDK 的 API 在两个版本中完全一致

## 参考文档

- [Spring Boot 3.x 自定义 Starter 指南](https://blog.csdn.net/a1256afafaafr/article/details/147768713)
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

