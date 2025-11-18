// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.auth;

import com.coze.loop.exception.AuthException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.internal.ValidationUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JWT OAuth authentication with automatic token refresh.
 * This implementation is thread-safe and automatically refreshes tokens.
 */
public class JWTOAuthAuth implements Auth {
    private static final Logger logger = LoggerFactory.getLogger(JWTOAuthAuth.class);
    private static final String AUTH_TYPE = "Bearer";
    private static final long TOKEN_EXPIRY_MINUTES = 55; // 55 minutes, JWT typically expires in 1 hour
    private static final long REFRESH_BUFFER_MINUTES = 5; // Refresh 5 minutes before expiry
    
    private final String clientId;
    private final PrivateKey privateKey;
    private final String publicKeyId;
    
    private volatile String currentToken;
    private volatile long tokenExpiryTime;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Create a JWTOAuthAuth instance.
     *
     * @param clientId the client ID
     * @param privateKeyPem the private key in PEM format (base64 encoded PKCS8)
     * @param publicKeyId the public key ID
     */
    public JWTOAuthAuth(String clientId, String privateKeyPem, String publicKeyId) {
        ValidationUtils.requireNonEmpty(clientId, "clientId");
        ValidationUtils.requireNonEmpty(privateKeyPem, "privateKeyPem");
        ValidationUtils.requireNonEmpty(publicKeyId, "publicKeyId");
        
        this.clientId = clientId;
        this.publicKeyId = publicKeyId;
        this.privateKey = parsePrivateKey(privateKeyPem);
        
        // Generate initial token
        refreshToken();
    }
    
    @Override
    public String getToken() {
        lock.readLock().lock();
        try {
            // Check if token needs refresh
            long currentTime = System.currentTimeMillis();
            if (shouldRefreshToken(currentTime)) {
                // Upgrade to write lock
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    // Double-check after acquiring write lock
                    if (shouldRefreshToken(System.currentTimeMillis())) {
                        refreshToken();
                    }
                    // Downgrade to read lock
                    lock.readLock().lock();
                } finally {
                    lock.writeLock().unlock();
                }
            }
            
            return currentToken;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public String getType() {
        return AUTH_TYPE;
    }
    
    /**
     * Check if token should be refreshed.
     */
    private boolean shouldRefreshToken(long currentTime) {
        return currentTime >= (tokenExpiryTime - REFRESH_BUFFER_MINUTES * 60 * 1000);
    }
    
    /**
     * Refresh the JWT token.
     * Must be called with write lock held.
     */
    private void refreshToken() {
        try {
            long currentTime = System.currentTimeMillis();
            long expiryTime = currentTime + TOKEN_EXPIRY_MINUTES * 60 * 1000;
            
            String jwt = Jwts.builder()
                .setIssuer(clientId)
                .setAudience("coze")
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expiryTime))
                .setHeaderParam("kid", publicKeyId)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
            
            this.currentToken = jwt;
            this.tokenExpiryTime = expiryTime;
            
            logger.debug("JWT token refreshed, expires at: {}", new Date(expiryTime));
        } catch (Exception e) {
            throw new AuthException(ErrorCode.AUTH_FAILED, "Failed to generate JWT token", e);
        }
    }
    
    /**
     * Parse private key from PEM format.
     */
    private PrivateKey parsePrivateKey(String privateKeyPem) {
        try {
            // Remove PEM header/footer and whitespace
            String privateKeyContent = privateKeyPem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
            
            // Decode base64
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            
            // Generate private key
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new AuthException(ErrorCode.AUTH_FAILED, "Failed to parse private key", e);
        }
    }
}

