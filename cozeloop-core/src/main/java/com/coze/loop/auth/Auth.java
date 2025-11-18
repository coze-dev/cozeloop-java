// Copyright (c) 2025 Bytedance Ltd. and/or its affiliates
// SPDX-License-Identifier: MIT

package com.coze.loop.auth;

/**
 * Authentication interface for CozeLoop SDK.
 */
public interface Auth {
    /**
     * Get the authentication token.
     *
     * @return the authentication token
     * @throws com.coze.loop.exception.AuthException if authentication fails
     */
    String getToken();
    
    /**
     * Get the authentication type (e.g., "Bearer", "JWT").
     *
     * @return the authentication type
     */
    String getType();
}

