package com.coze.loop.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class CozeLoopLogger implements Logger {

  private static final String PREFIX = "[cozeloop] ";
  private final Logger delegate;

  private CozeLoopLogger(Logger delegate) {
    this.delegate = delegate;
  }

  public static Logger getLogger(Class<?> clazz) {
    return new CozeLoopLogger(LoggerFactory.getLogger(clazz));
  }

  public static Logger getLogger(String name) {
    return new CozeLoopLogger(LoggerFactory.getLogger(name));
  }

  private String addPrefix(String msg) {
    return PREFIX + msg;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return delegate.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    delegate.trace(addPrefix(msg));
  }

  @Override
  public void trace(String format, Object arg) {
    delegate.trace(addPrefix(format), arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    delegate.trace(addPrefix(format), arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    delegate.trace(addPrefix(format), arguments);
  }

  @Override
  public void trace(String msg, Throwable t) {
    delegate.trace(addPrefix(msg), t);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return delegate.isTraceEnabled(marker);
  }

  @Override
  public void trace(Marker marker, String msg) {
    delegate.trace(marker, addPrefix(msg));
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    delegate.trace(marker, addPrefix(format), arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    delegate.trace(marker, addPrefix(format), arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    delegate.trace(marker, addPrefix(format), argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    delegate.trace(marker, addPrefix(msg), t);
  }

  @Override
  public boolean isDebugEnabled() {
    return delegate.isDebugEnabled();
  }

  @Override
  public void debug(String msg) {
    delegate.debug(addPrefix(msg));
  }

  @Override
  public void debug(String format, Object arg) {
    delegate.debug(addPrefix(format), arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    delegate.debug(addPrefix(format), arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    delegate.debug(addPrefix(format), arguments);
  }

  @Override
  public void debug(String msg, Throwable t) {
    delegate.debug(addPrefix(msg), t);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return delegate.isDebugEnabled(marker);
  }

  @Override
  public void debug(Marker marker, String msg) {
    delegate.debug(marker, addPrefix(msg));
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    delegate.debug(marker, addPrefix(format), arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    delegate.debug(marker, addPrefix(format), arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    delegate.debug(marker, addPrefix(format), arguments);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    delegate.debug(marker, addPrefix(msg), t);
  }

  @Override
  public boolean isInfoEnabled() {
    return delegate.isInfoEnabled();
  }

  @Override
  public void info(String msg) {
    delegate.info(addPrefix(msg));
  }

  @Override
  public void info(String format, Object arg) {
    delegate.info(addPrefix(format), arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    delegate.info(addPrefix(format), arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    delegate.info(addPrefix(format), arguments);
  }

  @Override
  public void info(String msg, Throwable t) {
    delegate.info(addPrefix(msg), t);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return delegate.isInfoEnabled(marker);
  }

  @Override
  public void info(Marker marker, String msg) {
    delegate.info(marker, addPrefix(msg));
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    delegate.info(marker, addPrefix(format), arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    delegate.info(marker, addPrefix(format), arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    delegate.info(marker, addPrefix(format), arguments);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    delegate.info(marker, addPrefix(msg), t);
  }

  @Override
  public boolean isWarnEnabled() {
    return delegate.isWarnEnabled();
  }

  @Override
  public void warn(String msg) {
    delegate.warn(addPrefix(msg));
  }

  @Override
  public void warn(String format, Object arg) {
    delegate.warn(addPrefix(format), arg);
  }

  @Override
  public void warn(String format, Object... arguments) {
    delegate.warn(addPrefix(format), arguments);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    delegate.warn(addPrefix(format), arg1, arg2);
  }

  @Override
  public void warn(String msg, Throwable t) {
    delegate.warn(addPrefix(msg), t);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return delegate.isWarnEnabled(marker);
  }

  @Override
  public void warn(Marker marker, String msg) {
    delegate.warn(marker, addPrefix(msg));
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    delegate.warn(marker, addPrefix(format), arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    delegate.warn(marker, addPrefix(format), arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    delegate.warn(marker, addPrefix(format), arguments);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    delegate.warn(marker, addPrefix(msg), t);
  }

  @Override
  public boolean isErrorEnabled() {
    return delegate.isErrorEnabled();
  }

  @Override
  public void error(String msg) {
    delegate.error(addPrefix(msg));
  }

  @Override
  public void error(String format, Object arg) {
    delegate.error(addPrefix(format), arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    delegate.error(addPrefix(format), arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    delegate.error(addPrefix(format), arguments);
  }

  @Override
  public void error(String msg, Throwable t) {
    delegate.error(addPrefix(msg), t);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return delegate.isErrorEnabled(marker);
  }

  @Override
  public void error(Marker marker, String msg) {
    delegate.error(marker, addPrefix(msg));
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    delegate.error(marker, addPrefix(format), arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    delegate.error(marker, addPrefix(format), arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    delegate.error(marker, addPrefix(format), arguments);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    delegate.error(marker, addPrefix(msg), t);
  }
}
