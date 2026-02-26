package com.coze.loop.spring.autoconfigure;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coze.loop.client.CozeLoopClient;
import com.coze.loop.client.CozeLoopClientBuilder;
import com.coze.loop.config.CozeLoopConfig;
import com.coze.loop.http.HttpConfig;
import com.coze.loop.internal.CozeLoopLogger;
import com.coze.loop.prompt.PromptCache;
import com.coze.loop.spring.aop.CozeTraceAspect;
import com.coze.loop.spring.config.CozeLoopProperties;
import com.coze.loop.trace.CozeLoopTracerProvider;

/**
 * Auto-configuration for CozeLoop Spring Boot integration. Supports both Spring Boot 2.x and 3.x.
 *
 * <p>For Spring Boot 3.x: Uses AutoConfiguration.imports file For Spring Boot 2.x: Uses
 * spring.factories file
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CozeLoopClient.class)
@EnableConfigurationProperties(CozeLoopProperties.class)
@ConditionalOnProperty(prefix = "cozeloop", name = "workspace-id")
public class CozeLoopAutoConfiguration {
  private static final Logger logger = CozeLoopLogger.getLogger(CozeLoopAutoConfiguration.class);

  /** Create CozeLoopClient bean. */
  @Bean
  @ConditionalOnMissingBean
  public CozeLoopClient cozeLoopClient(CozeLoopProperties properties) {
    logger.info("Initializing CozeLoop client with workspace: {}", properties.getWorkspaceId());

    // Build configuration
    CozeLoopConfig config =
        CozeLoopConfig.builder()
            .workspaceId(properties.getWorkspaceId())
            .serviceName(
                properties.getServiceName() != null
                    ? properties.getServiceName()
                    : "spring-boot-app")
            .baseUrl(properties.getBaseUrl())
            .httpConfig(buildHttpConfig(properties.getHttp()))
            .traceConfig(buildTraceConfig(properties.getTrace()))
            .promptCacheConfig(buildPromptCacheConfig(properties.getPrompt().getCache()))
            .build();

    // Build client
    CozeLoopClientBuilder builder = new CozeLoopClientBuilder().config(config);

    // Configure authentication
    if (properties.getAuth().getToken() != null && !properties.getAuth().getToken().isEmpty()) {
      builder.tokenAuth(properties.getAuth().getToken());
    } else if (properties.getAuth().getJwt().getClientId() != null) {
      builder.jwtOAuth(
          properties.getAuth().getJwt().getClientId(),
          properties.getAuth().getJwt().getPrivateKey(),
          properties.getAuth().getJwt().getPublicKeyId());
    } else {
      throw new IllegalArgumentException(
          "Either cozeloop.auth.token or cozeloop.auth.jwt must be configured");
    }

    return builder.build();
  }

  /** Create CozeTraceAspect bean if trace is enabled. */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "cozeloop.trace",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public CozeTraceAspect cozeTraceAspect(CozeLoopClient client) {
    logger.info("Enabling @CozeTrace annotation support");
    return new CozeTraceAspect(client);
  }

  /** Build HttpConfig from properties. */
  private HttpConfig buildHttpConfig(CozeLoopProperties.Http http) {
    return HttpConfig.builder()
        .connectTimeoutSeconds(http.getConnectTimeoutSeconds())
        .readTimeoutSeconds(http.getReadTimeoutSeconds())
        .writeTimeoutSeconds(http.getWriteTimeoutSeconds())
        .maxRetries(http.getMaxRetries())
        .build();
  }

  /** Build TraceConfig from properties. */
  private CozeLoopTracerProvider.TraceConfig buildTraceConfig(CozeLoopProperties.Trace trace) {
    return CozeLoopTracerProvider.TraceConfig.builder()
        .maxQueueSize(trace.getMaxQueueSize())
        .batchSize(trace.getBatchSize())
        .scheduleDelayMillis(trace.getScheduleDelayMillis())
        .build();
  }

  /** Build PromptCacheConfig from properties. */
  private PromptCache.PromptCacheConfig buildPromptCacheConfig(
      CozeLoopProperties.Prompt.Cache cache) {
    return PromptCache.PromptCacheConfig.builder()
        .maxSize(cache.getMaxSize())
        .expireAfterWriteMinutes(cache.getExpireAfterWriteMinutes())
        .refreshAfterWriteMinutes(cache.getRefreshAfterWriteMinutes())
        .build();
  }
}
