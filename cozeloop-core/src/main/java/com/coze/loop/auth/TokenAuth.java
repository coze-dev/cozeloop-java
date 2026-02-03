package com.coze.loop.auth;

import com.coze.loop.exception.AuthException;
import com.coze.loop.exception.ErrorCode;
import com.coze.loop.internal.ValidationUtils;

/** Simple token-based authentication. */
public class TokenAuth implements Auth {
  private static final String AUTH_TYPE = "Bearer";

  private final String token;

  /**
   * Create a TokenAuth instance.
   *
   * @param token the access token
   */
  public TokenAuth(String token) {
    ValidationUtils.requireNonEmpty(token, "token");
    this.token = token;
  }

  @Override
  public String getToken() {
    if (token == null || token.isEmpty()) {
      throw new AuthException(ErrorCode.AUTH_FAILED, "Token is empty");
    }
    return token;
  }

  @Override
  public String getType() {
    return AUTH_TYPE;
  }
}
