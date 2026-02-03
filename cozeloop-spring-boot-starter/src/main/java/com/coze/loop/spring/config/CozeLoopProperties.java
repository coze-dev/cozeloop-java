package com.coze.loop.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for CozeLoop Spring Boot integration. */
@ConfigurationProperties(prefix = "cozeloop")
public class CozeLoopProperties {

  /** Workspace ID (required). */
  private String workspaceId;

  /** Service name (default: application name). */
  private String serviceName;

  /** Base URL (default: https://api.coze.cn). */
  private String baseUrl = "https://api.coze.cn";

  /** Authentication configuration. */
  private Auth auth = new Auth();

  /** HTTP configuration. */
  private Http http = new Http();

  /** Trace configuration. */
  private Trace trace = new Trace();

  /** Prompt configuration. */
  private Prompt prompt = new Prompt();

  // Getters and Setters
  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public Auth getAuth() {
    return auth;
  }

  public void setAuth(Auth auth) {
    this.auth = auth;
  }

  public Http getHttp() {
    return http;
  }

  public void setHttp(Http http) {
    this.http = http;
  }

  public Trace getTrace() {
    return trace;
  }

  public void setTrace(Trace trace) {
    this.trace = trace;
  }

  public Prompt getPrompt() {
    return prompt;
  }

  public void setPrompt(Prompt prompt) {
    this.prompt = prompt;
  }

  /** Authentication properties. */
  public static class Auth {
    private String token;
    private Jwt jwt = new Jwt();

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }

    public Jwt getJwt() {
      return jwt;
    }

    public void setJwt(Jwt jwt) {
      this.jwt = jwt;
    }

    public static class Jwt {
      private String clientId;
      private String privateKey;
      private String publicKeyId;

      public String getClientId() {
        return clientId;
      }

      public void setClientId(String clientId) {
        this.clientId = clientId;
      }

      public String getPrivateKey() {
        return privateKey;
      }

      public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
      }

      public String getPublicKeyId() {
        return publicKeyId;
      }

      public void setPublicKeyId(String publicKeyId) {
        this.publicKeyId = publicKeyId;
      }
    }
  }

  /** HTTP properties. */
  public static class Http {
    private int connectTimeoutSeconds = 30;
    private int readTimeoutSeconds = 60;
    private int writeTimeoutSeconds = 60;
    private int maxRetries = 3;

    public int getConnectTimeoutSeconds() {
      return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
      this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
      return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
      this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public int getWriteTimeoutSeconds() {
      return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(int writeTimeoutSeconds) {
      this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    public int getMaxRetries() {
      return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
    }
  }

  /** Trace properties. */
  public static class Trace {
    private boolean enabled = true;
    private int maxQueueSize = 2048;
    private int batchSize = 512;
    private long scheduleDelayMillis = 5000;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public int getMaxQueueSize() {
      return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
    }

    public int getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
    }

    public long getScheduleDelayMillis() {
      return scheduleDelayMillis;
    }

    public void setScheduleDelayMillis(long scheduleDelayMillis) {
      this.scheduleDelayMillis = scheduleDelayMillis;
    }
  }

  /** Prompt properties. */
  public static class Prompt {
    private Cache cache = new Cache();

    public Cache getCache() {
      return cache;
    }

    public void setCache(Cache cache) {
      this.cache = cache;
    }

    public static class Cache {
      private long maxSize = 1000;
      private long expireAfterWriteMinutes = 60;
      private long refreshAfterWriteMinutes = 30;

      public long getMaxSize() {
        return maxSize;
      }

      public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
      }

      public long getExpireAfterWriteMinutes() {
        return expireAfterWriteMinutes;
      }

      public void setExpireAfterWriteMinutes(long expireAfterWriteMinutes) {
        this.expireAfterWriteMinutes = expireAfterWriteMinutes;
      }

      public long getRefreshAfterWriteMinutes() {
        return refreshAfterWriteMinutes;
      }

      public void setRefreshAfterWriteMinutes(long refreshAfterWriteMinutes) {
        this.refreshAfterWriteMinutes = refreshAfterWriteMinutes;
      }
    }
  }
}
