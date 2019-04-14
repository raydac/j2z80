/* 
 * Copyright 2019 Igor Maznitsa.
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
 * An Exception to be thrown for problems during boot class processing problems
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 * @see AbstractBootClass
 */
public class BootClassException extends RuntimeException {
  private static final long serialVersionUID = 982394812L;

  private final String className;
  private final String methodOrFieldName;
  private final String signature;

  /**
   * The Constructor
   *
   * @param message           the message for the exception
   * @param className         the boot class name
   * @param methodOrFieldName the source method or field
   * @param signature         the method or field signature
   */
  public BootClassException(final String message, final String className, final String methodOrFieldName, final String signature) {
    super(message);
    this.className = className;
    this.methodOrFieldName = methodOrFieldName;
    this.signature = signature;
  }

  /**
   * Get the exception source class name
   *
   * @return the exception source class name
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Get the exception source method or field name
   *
   * @return the source method or field name
   */
  public String getMethodOrFieldName() {
    return this.methodOrFieldName;
  }

  /**
   * Get the exception source method or field signature
   *
   * @return the source method or field signature
   */
  public String getSignature() {
    return this.signature;
  }

  @Override
  public String toString() {
    return this.className + "." + this.methodOrFieldName + " " + this.signature;
  }
}
