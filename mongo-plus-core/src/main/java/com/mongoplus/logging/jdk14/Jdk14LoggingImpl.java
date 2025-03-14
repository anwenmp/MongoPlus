/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.mongoplus.logging.jdk14;

import com.mongoplus.logging.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Clinton Begin
 */
public class Jdk14LoggingImpl implements Log {

  private final Logger log;

  public Jdk14LoggingImpl(String clazz) {
    log = Logger.getLogger(clazz);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isLoggable(Level.FINE);
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isLoggable(Level.FINER);
  }

  @Override
  public void info(String s) {
    log.log(Level.INFO, s);
  }

  @Override
  public void error(String s, Throwable e) {
    log.log(Level.SEVERE, s, e);
  }

  @Override
  public void error(String s, Object arg) {
    log.log(Level.SEVERE, s, arg);
  }

  @Override
  public void error(String s, Object arg1, Object arg2) {
    log.log(Level.SEVERE, s, new Object[]{arg1, arg2});
  }

  @Override
  public void error(String s) {
    log.log(Level.SEVERE, s);
  }

  @Override
  public void debug(String s) {
    log.log(Level.FINE, s);
  }

  @Override
  public void debug(String format, Object arg) {
    log.log(Level.FINE, format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    log.log(Level.FINE, format, new Object[]{arg1, arg2});
  }

  @Override
  public void trace(String s) {
    log.log(Level.FINER, s);
  }

  @Override
  public void warn(String s) {
    log.log(Level.WARNING, s);
  }

  @Override
  public void warn(String s, Object arg) {
    log.log(Level.WARNING, s, arg);
  }

  @Override
  public void warn(String s, Object arg1, Object arg2) {
    log.log(Level.WARNING, s, new Object[]{arg1, arg2});
  }

}
