/*
 * Copyright 2012-2026 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.j2z80.bootstrap;

/**
 * Signals that a bootstrap class does not implement a Java API method or field in a way that the
 * translator can emit as Z80 assembly. This runtime exception is raised when a native or synthetic
 * bootstrap type is asked to generate code for an unsupported operation.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 * @see AbstractBootstrapClass
 */
public class BootClassException extends RuntimeException {
  private static final long serialVersionUID = 982394812L;

  private final String className;
  private final String methodOrFieldName;
  private final String signature;

  /**
   * Creates a new bootstrap exception with the exact unsupported operation details.
   *
   * @param message the human-readable exception message
   * @param className the bootstrap class that triggered the failure
   * @param methodOrFieldName the method or field name that was not supported
   * @param signature the JVM method or field signature that caused the failure
   */
  public BootClassException(final String message, final String className,
                            final String methodOrFieldName, final String signature) {
    super(message);
    this.className = className;
    this.methodOrFieldName = methodOrFieldName;
    this.signature = signature;
  }

  /**
   * Returns the bootstrap class name that generated the failure.
   *
   * @return the class name associated with the unsupported operation
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Returns the method or field name that triggered the unsupported operation.
   *
   * @return the operation source name
   */
  public String getMethodOrFieldName() {
    return this.methodOrFieldName;
  }

  /**
   * Returns the JVM signature of the unsupported method or field.
   *
   * @return the Java signature string
   */
  public String getSignature() {
    return this.signature;
  }

  @Override
  public String toString() {
    return this.className + "." + this.methodOrFieldName + " " + this.signature;
  }
}
