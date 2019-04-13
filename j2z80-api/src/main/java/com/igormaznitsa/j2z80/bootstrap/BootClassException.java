/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
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
