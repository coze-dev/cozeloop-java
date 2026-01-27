package com.coze.loop.http;

/**
 * HTTP client configuration.
 */
public class HttpConfig {
    private int connectTimeoutSeconds = 30;
    private int readTimeoutSeconds = 60;
    private int writeTimeoutSeconds = 60;
    private int maxIdleConnections = 5;
    private int keepAliveDurationMinutes = 5;
    private int maxRetries = 3;
    
    public HttpConfig() {
    }
    
    // Getters and Setters
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
    
    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }
    
    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }
    
    public int getKeepAliveDurationMinutes() {
        return keepAliveDurationMinutes;
    }
    
    public void setKeepAliveDurationMinutes(int keepAliveDurationMinutes) {
        this.keepAliveDurationMinutes = keepAliveDurationMinutes;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final HttpConfig config = new HttpConfig();
        
        public Builder connectTimeoutSeconds(int seconds) {
            config.connectTimeoutSeconds = seconds;
            return this;
        }
        
        public Builder readTimeoutSeconds(int seconds) {
            config.readTimeoutSeconds = seconds;
            return this;
        }
        
        public Builder writeTimeoutSeconds(int seconds) {
            config.writeTimeoutSeconds = seconds;
            return this;
        }
        
        public Builder maxIdleConnections(int max) {
            config.maxIdleConnections = max;
            return this;
        }
        
        public Builder keepAliveDurationMinutes(int minutes) {
            config.keepAliveDurationMinutes = minutes;
            return this;
        }
        
        public Builder maxRetries(int retries) {
            config.maxRetries = retries;
            return this;
        }
        
        public HttpConfig build() {
            return config;
        }
    }
}

