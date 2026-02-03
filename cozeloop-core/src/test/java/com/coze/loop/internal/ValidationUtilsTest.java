package com.coze.loop.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.coze.loop.exception.CozeLoopException;
import com.coze.loop.exception.ErrorCode;

/** Unit tests for ValidationUtils. */
class ValidationUtilsTest {

  @Test
  void testRequireNonEmptyWithValidString() {
    ValidationUtils.requireNonEmpty("valid", "param");
    // Should not throw
  }

  @Test
  void testRequireNonEmptyWithNull() {
    assertThatThrownBy(() -> ValidationUtils.requireNonEmpty(null, "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
              assertThat(ex.getMessage()).contains("param");
            });
  }

  @Test
  void testRequireNonEmptyWithEmptyString() {
    assertThatThrownBy(() -> ValidationUtils.requireNonEmpty("", "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
            });
  }

  @Test
  void testRequireNonEmptyWithWhitespaceOnly() {
    assertThatThrownBy(() -> ValidationUtils.requireNonEmpty("   ", "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
            });
  }

  @Test
  void testRequireNonNullWithValidObject() {
    ValidationUtils.requireNonNull("valid", "param");
    // Should not throw
  }

  @Test
  void testRequireNonNullWithNull() {
    assertThatThrownBy(() -> ValidationUtils.requireNonNull(null, "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
              assertThat(ex.getMessage()).contains("param");
            });
  }

  @Test
  void testRequirePositiveWithPositiveNumber() {
    ValidationUtils.requirePositive(1, "param");
    ValidationUtils.requirePositive(100, "param");
    // Should not throw
  }

  @Test
  void testRequirePositiveWithZero() {
    assertThatThrownBy(() -> ValidationUtils.requirePositive(0, "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
            });
  }

  @Test
  void testRequirePositiveWithNegativeNumber() {
    assertThatThrownBy(() -> ValidationUtils.requirePositive(-1, "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
            });
  }

  @Test
  void testRequireNonNegativeWithNonNegativeNumber() {
    ValidationUtils.requireNonNegative(0, "param");
    ValidationUtils.requireNonNegative(1, "param");
    ValidationUtils.requireNonNegative(100, "param");
    // Should not throw
  }

  @Test
  void testRequireNonNegativeWithNegativeNumber() {
    assertThatThrownBy(() -> ValidationUtils.requireNonNegative(-1, "param"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
            });
  }

  @Test
  void testRequireWithTrueCondition() {
    ValidationUtils.require(true, "test message");
    // Should not throw
  }

  @Test
  void testRequireWithFalseCondition() {
    assertThatThrownBy(() -> ValidationUtils.require(false, "test message"))
        .isInstanceOf(CozeLoopException.class)
        .satisfies(
            e -> {
              CozeLoopException ex = (CozeLoopException) e;
              assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAM);
              assertThat(ex.getMessage()).contains("test message");
            });
  }
}
