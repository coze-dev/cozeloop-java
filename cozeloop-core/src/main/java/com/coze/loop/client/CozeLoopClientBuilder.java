package com.coze.loop.client;

import com.coze.loop.auth.Auth;
import com.coze.loop.auth.JWTOAuthAuth;
import com.coze.loop.auth.TokenAuth;
import com.coze.loop.config.CozeLoopConfig;
import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.http.HttpClient;
import com.coze.loop.internal.ConfigUtils;
import com.coze.loop.internal.ValidationUtils;
import com.coze.loop.prompt.PromptProvider;
import com.coze.loop.trace.CozeLoopTracerProvider;

/**
 * Builder for creating CozeLoopClient instances.
 */
public class CozeLoopClientBuilder {
    private CozeLoopConfig config;
    private Auth auth;
    
    public CozeLoopClientBuilder() {
        this.config = CozeLoopConfig.builder().build();
        loadByProperties();
    }

    private void loadByProperties() {
        // Load workspace ID
        String workspaceId = ConfigUtils.get("cozeloop.workspace-id");
        if (workspaceId != null) {
            this.config.setWorkspaceId(workspaceId);
        }

        // Load service name
        String serviceName = ConfigUtils.get("cozeloop.service-name");
        if (serviceName != null) {
            this.config.setServiceName(serviceName);
        }

        // Load auth token
        String token = ConfigUtils.get("cozeloop.auth.token");
        if (token != null) {
            this.tokenAuth(token);
        }
    }
    
    /**
     * Set workspace ID (required).
     *
     * @param workspaceId the workspace ID
     * @return this builder
     */
    public CozeLoopClientBuilder workspaceId(String workspaceId) {
        config.setWorkspaceId(workspaceId);
        return this;
    }
    
    /**
     * Set service name (optional, default: "cozeloop-java-app").
     *
     * @param serviceName the service name
     * @return this builder
     */
    public CozeLoopClientBuilder serviceName(String serviceName) {
        config.setServiceName(serviceName);
        return this;
    }
    
    /**
     * Set base URL (optional, default: "https://api.coze.cn").
     *
     * @param baseUrl the base URL
     * @return this builder
     */
    public CozeLoopClientBuilder baseUrl(String baseUrl) {
        config.setBaseUrl(baseUrl);
        return this;
    }
    
    /**
     * Set configuration (optional).
     *
     * @param config the configuration
     * @return this builder
     */
    public CozeLoopClientBuilder config(CozeLoopConfig config) {
        this.config = config;
        return this;
    }
    
    /**
     * Use token authentication.
     *
     * @param token the access token
     * @return this builder
     */
    public CozeLoopClientBuilder tokenAuth(String token) {
        this.auth = new TokenAuth(token);
        return this;
    }
    
    /**
     * Use JWT OAuth authentication.
     *
     * @param clientId the client ID
     * @param privateKey the private key (PEM format)
     * @param publicKeyId the public key ID
     * @return this builder
     */
    public CozeLoopClientBuilder jwtOAuth(String clientId, String privateKey, String publicKeyId) {
        this.auth = new JWTOAuthAuth(clientId, privateKey, publicKeyId);
        return this;
    }
    
    /**
     * Use custom authentication.
     *
     * @param auth the authentication provider
     * @return this builder
     */
    public CozeLoopClientBuilder auth(Auth auth) {
        this.auth = auth;
        return this;
    }
    
    /**
     * Build the CozeLoopClient instance.
     *
     * @return CozeLoopClient instance
     */
    public CozeLoopClient build() {
        // Validate required fields
        ValidationUtils.requireNonEmpty(config.getWorkspaceId(), "workspaceId");
        ValidationUtils.requireNonNull(auth, "auth");
        
        try {
            // Create TracerProvider using OTLP/HTTP exporter
            CozeLoopTracerProvider tracerProvider = CozeLoopTracerProvider.create(
                    auth,
                config.getSpanEndpoint(),
                config.getWorkspaceId(),
                config.getServiceName(),
                config.getTraceConfig()
            );
            
            // Create HTTP client with trace propagators
            HttpClient httpClient = new HttpClient(auth, config.getHttpConfig(), tracerProvider.getPropagators());
            
            // Configure JWT auth if used
            if (auth instanceof JWTOAuthAuth) {
                JWTOAuthAuth jwtAuth = (JWTOAuthAuth) auth;
                jwtAuth.setBaseUrl(config.getBaseUrl());
                jwtAuth.setHttpClient(new HttpClient(null, config.getHttpConfig(), tracerProvider.getPropagators()));
            }
            
            // Create PromptProvider
            PromptProvider promptProvider = new PromptProvider(
                httpClient,
                config.getPromptEndpoint(),
                config.getExecutePromptEndpoint(),
                config.getExecuteStreamingPromptEndpoint(),
                config.getWorkspaceId(),
                config.getPromptCacheConfig()
            );
            
            // Create client implementation
            return new CozeLoopClientImpl(
                config.getWorkspaceId(),
                tracerProvider,
                promptProvider,
                httpClient
            );
        } catch (Exception e) {
            throw new CozeLoopException(ErrorCode.INTERNAL_ERROR,
                "Failed to create CozeLoopClient", e);
        }
    }
}

