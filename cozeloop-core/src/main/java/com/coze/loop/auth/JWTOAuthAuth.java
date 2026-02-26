package com.coze.loop.auth;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;

import com.coze.loop.exception.AuthException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.http.HttpClient;
import com.coze.loop.internal.CozeLoopLogger;
import com.coze.loop.internal.JsonUtils;
import com.coze.loop.internal.ValidationUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT OAuth authentication with automatic token refresh. This implementation is thread-safe and
 * automatically refreshes tokens.
 */
public class JWTOAuthAuth implements Auth {
  private static final Logger logger = CozeLoopLogger.getLogger(JWTOAuthAuth.class);
  private static final String AUTH_TYPE = "Bearer";
  private static final String GRANT_TYPE_JWT = "urn:ietf:params:oauth:grant-type:jwt-bearer";
  private static final String TOKEN_PATH = "/api/permission/oauth2/token";
  private static final long REFRESH_BUFFER_MS = 5 * 60 * 1000; // 5 minutes
  private static final long DEFAULT_JWT_TTL_MS = 15 * 60 * 1000; // 15 minutes

  private final String clientId;
  private final PrivateKey privateKey;
  private final String publicKeyId;
  private String baseUrl;
  private HttpClient httpClient;

  private volatile String currentToken;
  private volatile long tokenExpiryTime;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Create a JWTOAuthAuth instance with default base URL.
   *
   * @param clientId the client ID
   * @param privateKeyPem the private key in PEM format (base64 encoded PKCS8)
   * @param publicKeyId the public key ID
   */
  public JWTOAuthAuth(String clientId, String privateKeyPem, String publicKeyId) {
    this(clientId, privateKeyPem, publicKeyId, "https://api.coze.cn");
  }

  /**
   * Create a JWTOAuthAuth instance with custom base URL.
   *
   * @param clientId the client ID
   * @param privateKeyPem the private key in PEM format (base64 encoded PKCS8)
   * @param publicKeyId the public key ID
   * @param baseUrl the base URL for token refresh
   */
  public JWTOAuthAuth(String clientId, String privateKeyPem, String publicKeyId, String baseUrl) {
    ValidationUtils.requireNonEmpty(clientId, "clientId");
    ValidationUtils.requireNonEmpty(privateKeyPem, "privateKeyPem");
    ValidationUtils.requireNonEmpty(publicKeyId, "publicKeyId");

    this.clientId = clientId;
    this.publicKeyId = publicKeyId;
    this.privateKey = parsePrivateKey(privateKeyPem);
    this.baseUrl = baseUrl != null ? baseUrl : "https://api.coze.cn";
    this.httpClient = new HttpClient(null);
  }

  /**
   * Set the base URL for token refresh.
   *
   * @param baseUrl the base URL
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Set the HttpClient for token refresh.
   *
   * @param httpClient the HttpClient
   */
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
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
    return AUTH_TYPE + " ";
  }

  /** Check if token should be refreshed. */
  private boolean shouldRefreshToken(long currentTime) {
    return currentToken == null || currentTime >= (tokenExpiryTime - REFRESH_BUFFER_MS);
  }

  /**
   * Refresh the OAuth token by exchanging a JWT for an access token. Must be called with write lock
   * held.
   */
  private void refreshToken() {
    try {
      long currentTime = System.currentTimeMillis();
      String host = new java.net.URL(baseUrl).getHost();

      // 1. Generate JWT
      String jwt =
          Jwts.builder()
              .setIssuer(clientId)
              .setAudience(host)
              .setIssuedAt(new Date(currentTime))
              .setExpiration(new Date(currentTime + DEFAULT_JWT_TTL_MS))
              .setHeaderParam("kid", publicKeyId)
              .setHeaderParam("typ", "JWT")
              .setHeaderParam("alg", "RS256")
              .setId(UUID.randomUUID().toString())
              .signWith(privateKey, SignatureAlgorithm.RS256)
              .compact();

      // 2. Exchange JWT for Access Token using HttpClient
      Map<String, Object> body = new HashMap<>();
      body.put("client_id", clientId);
      body.put("grant_type", GRANT_TYPE_JWT);

      Map<String, String> headers = new HashMap<>();
      headers.put("Authorization", this.getType() + jwt);

      String responseJson = httpClient.post(baseUrl + TOKEN_PATH, body, headers);
      TokenResponse resp = JsonUtils.fromJson(responseJson, TokenResponse.class);

      if (resp == null || resp.accessToken == null) {
        throw new AuthException(ErrorCode.AUTH_FAILED, "Invalid response from token endpoint");
      }

      this.currentToken = resp.accessToken;
      this.tokenExpiryTime = System.currentTimeMillis() + resp.expiresIn * 1000;

      logger.debug("OAuth token refreshed, expires at: {}", new Date(this.tokenExpiryTime));
    } catch (Exception e) {
      throw new AuthException(ErrorCode.AUTH_FAILED, "Failed to refresh OAuth token", e);
    }
  }

  private static class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;
  }

  /** Parse private key from PEM format. */
  private PrivateKey parsePrivateKey(String privateKeyPem) {
    try {
      // Remove PEM header/footer and whitespace
      String privateKeyContent =
          privateKeyPem
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
