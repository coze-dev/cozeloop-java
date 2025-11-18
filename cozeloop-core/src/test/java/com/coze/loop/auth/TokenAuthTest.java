package com.coze.loop.auth;

import com.coze.loop.exception.CozeLoopException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TokenAuth.
 */
class TokenAuthTest {

    @Test
    void testConstructorWithValidToken() {
        TokenAuth auth = new TokenAuth("test-token");
        
        assertThat(auth.getToken()).isEqualTo("test-token");
        assertThat(auth.getType()).isEqualTo("Bearer");
    }

    @Test
    void testConstructorWithNullToken() {
        assertThatThrownBy(() -> new TokenAuth(null))
            .isInstanceOf(CozeLoopException.class)
            .satisfies(e -> {
                CozeLoopException ex = (CozeLoopException) e;
                assertThat(ex.getErrorCode()).isEqualTo(com.coze.loop.exception.ErrorCode.INVALID_PARAM);
            });
    }

    @Test
    void testConstructorWithEmptyToken() {
        assertThatThrownBy(() -> new TokenAuth(""))
            .isInstanceOf(CozeLoopException.class)
            .satisfies(e -> {
                CozeLoopException ex = (CozeLoopException) e;
                assertThat(ex.getErrorCode()).isEqualTo(com.coze.loop.exception.ErrorCode.INVALID_PARAM);
            });
    }

    @Test
    void testConstructorWithWhitespaceToken() {
        assertThatThrownBy(() -> new TokenAuth("   "))
            .isInstanceOf(CozeLoopException.class)
            .satisfies(e -> {
                CozeLoopException ex = (CozeLoopException) e;
                assertThat(ex.getErrorCode()).isEqualTo(com.coze.loop.exception.ErrorCode.INVALID_PARAM);
            });
    }

    @Test
    void testGetToken() {
        TokenAuth auth = new TokenAuth("my-token-123");
        assertThat(auth.getToken()).isEqualTo("my-token-123");
    }

    @Test
    void testGetType() {
        TokenAuth auth = new TokenAuth("token");
        assertThat(auth.getType()).isEqualTo("Bearer");
    }
}

