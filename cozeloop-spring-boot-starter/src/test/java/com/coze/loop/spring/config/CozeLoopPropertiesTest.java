package com.coze.loop.spring.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for CozeLoopProperties. */
class CozeLoopPropertiesTest {

  private CozeLoopProperties properties;

  @BeforeEach
  void setUp() {
    properties = new CozeLoopProperties();
  }

  @Test
  void testDefaultValues() {
    assertThat(properties.getBaseUrl()).isEqualTo("https://api.coze.cn");
    assertThat(properties.getWorkspaceId()).isNull();
    assertThat(properties.getServiceName()).isNull();
    assertThat(properties.getAuth()).isNotNull();
    assertThat(properties.getHttp()).isNotNull();
    assertThat(properties.getTrace()).isNotNull();
    assertThat(properties.getPrompt()).isNotNull();
  }

  @Test
  void testWorkspaceId() {
    properties.setWorkspaceId("test-workspace");
    assertThat(properties.getWorkspaceId()).isEqualTo("test-workspace");
  }

  @Test
  void testServiceName() {
    properties.setServiceName("test-service");
    assertThat(properties.getServiceName()).isEqualTo("test-service");
  }

  @Test
  void testBaseUrl() {
    properties.setBaseUrl("https://custom.url");
    assertThat(properties.getBaseUrl()).isEqualTo("https://custom.url");
  }

  @Test
  void testAuthToken() {
    CozeLoopProperties.Auth auth = new CozeLoopProperties.Auth();
    auth.setToken("test-token");
    properties.setAuth(auth);

    assertThat(properties.getAuth().getToken()).isEqualTo("test-token");
  }

  @Test
  void testAuthJwt() {
    CozeLoopProperties.Auth auth = new CozeLoopProperties.Auth();
    CozeLoopProperties.Auth.Jwt jwt = new CozeLoopProperties.Auth.Jwt();
    jwt.setClientId("test-client-id");
    jwt.setPrivateKey("test-private-key");
    jwt.setPublicKeyId("test-public-key-id");
    auth.setJwt(jwt);
    properties.setAuth(auth);

    assertThat(properties.getAuth().getJwt().getClientId()).isEqualTo("test-client-id");
    assertThat(properties.getAuth().getJwt().getPrivateKey()).isEqualTo("test-private-key");
    assertThat(properties.getAuth().getJwt().getPublicKeyId()).isEqualTo("test-public-key-id");
  }

  @Test
  void testHttpProperties() {
    CozeLoopProperties.Http http = new CozeLoopProperties.Http();
    http.setConnectTimeoutSeconds(10);
    http.setReadTimeoutSeconds(20);
    http.setWriteTimeoutSeconds(30);
    http.setMaxRetries(5);
    properties.setHttp(http);

    assertThat(properties.getHttp().getConnectTimeoutSeconds()).isEqualTo(10);
    assertThat(properties.getHttp().getReadTimeoutSeconds()).isEqualTo(20);
    assertThat(properties.getHttp().getWriteTimeoutSeconds()).isEqualTo(30);
    assertThat(properties.getHttp().getMaxRetries()).isEqualTo(5);
  }

  @Test
  void testHttpDefaultValues() {
    CozeLoopProperties.Http http = new CozeLoopProperties.Http();
    assertThat(http.getConnectTimeoutSeconds()).isEqualTo(30);
    assertThat(http.getReadTimeoutSeconds()).isEqualTo(60);
    assertThat(http.getWriteTimeoutSeconds()).isEqualTo(60);
    assertThat(http.getMaxRetries()).isEqualTo(3);
  }

  @Test
  void testTraceProperties() {
    CozeLoopProperties.Trace trace = new CozeLoopProperties.Trace();
    trace.setEnabled(false);
    trace.setMaxQueueSize(1024);
    trace.setBatchSize(256);
    trace.setScheduleDelayMillis(10000);
    properties.setTrace(trace);

    assertThat(properties.getTrace().isEnabled()).isFalse();
    assertThat(properties.getTrace().getMaxQueueSize()).isEqualTo(1024);
    assertThat(properties.getTrace().getBatchSize()).isEqualTo(256);
    assertThat(properties.getTrace().getScheduleDelayMillis()).isEqualTo(10000);
  }

  @Test
  void testTraceDefaultValues() {
    CozeLoopProperties.Trace trace = new CozeLoopProperties.Trace();
    assertThat(trace.isEnabled()).isTrue();
    assertThat(trace.getMaxQueueSize()).isEqualTo(2048);
    assertThat(trace.getBatchSize()).isEqualTo(512);
    assertThat(trace.getScheduleDelayMillis()).isEqualTo(5000);
  }

  @Test
  void testPromptCacheProperties() {
    CozeLoopProperties.Prompt.Cache cache = new CozeLoopProperties.Prompt.Cache();
    cache.setMaxSize(500);
    cache.setExpireAfterWriteMinutes(30);
    cache.setRefreshAfterWriteMinutes(15);

    CozeLoopProperties.Prompt prompt = new CozeLoopProperties.Prompt();
    prompt.setCache(cache);
    properties.setPrompt(prompt);

    assertThat(properties.getPrompt().getCache().getMaxSize()).isEqualTo(500);
    assertThat(properties.getPrompt().getCache().getExpireAfterWriteMinutes()).isEqualTo(30);
    assertThat(properties.getPrompt().getCache().getRefreshAfterWriteMinutes()).isEqualTo(15);
  }

  @Test
  void testPromptCacheDefaultValues() {
    CozeLoopProperties.Prompt.Cache cache = new CozeLoopProperties.Prompt.Cache();
    assertThat(cache.getMaxSize()).isEqualTo(1000);
    assertThat(cache.getExpireAfterWriteMinutes()).isEqualTo(60);
    assertThat(cache.getRefreshAfterWriteMinutes()).isEqualTo(30);
  }
}
