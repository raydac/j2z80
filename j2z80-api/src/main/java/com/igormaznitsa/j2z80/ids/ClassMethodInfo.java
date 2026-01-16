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
package com.igormaznitsa.j2z80.ids;

import java.util.Objects;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The Class describes a class method for inside translating operations.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ClassMethodInfo {

  private final ClassGen classInfo;
  private final Method methodInfo;
  private final int id;
  private MethodGen lazyMethodGen;

  /**
   * A Constructor
   *
   * @param classInfo  the ClassGen object contains the method
   * @param methodInfo the Method object
   * @param id         the ID of the method object
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final int id) {
    this.classInfo = classInfo;
    this.methodInfo = methodInfo;
    this.id = id;
  }

  /**
   * A Constructor
   *
   * @param classInfo  the ClassGen object contains the method
   * @param methodInfo the Method object
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo) {
    this(classInfo, methodInfo, -1);
  }

  /**
   * A Constructor
   *
   * @param classInfo  the ClassGen object contains the method
   * @param methodInfo the Method object describes the method
   * @param methodGen  the MethodGen object for the method
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final MethodGen methodGen) {
    this(classInfo, methodInfo, -1);
    this.lazyMethodGen = methodGen;
  }

  /**
   * Get UID for the method info
   *
   * @return the UID as integer
   */
  public int getUID() {
    return id;
  }

  /**
   * Get the ClassGen object saved by the info
   *
   * @return the ClassGen object saved bye the info
   */
  public ClassGen getClassInfo() {
    return this.classInfo;
  }

  /**
   * Get the Method object saved by the info
   *
   * @return the Method for the info object
   */
  public Method getMethodInfo() {
    return this.methodInfo;
  }

  /**
   * Get the package name (without the class name) for the class contains the method
   *
   * @return the class package information as String
   */
  public String getPackageName() {
    final String fullClassName = this.classInfo.getClassName();
    final int index = fullClassName.lastIndexOf('.');
    if (index < 0) {
      return "";
    } else {
      return fullClassName.substring(0, index);
    }
  }

  /**
   * Get the canonical class name.
   *
   * @return the canonical class name as String
   */
  public String getCanonicalClassName() {
    return this.classInfo.getClassName();
  }

  /**
   * Get only class name (package data excluded) for the class contains the method.
   *
   * @return the class name as String
   */
  public String getOnlyClassName() {
    final String fullClassName = this.classInfo.getClassName();
    final int index = fullClassName.lastIndexOf('.');
    if (index < 0) {
      return fullClassName;
    } else {
      return fullClassName.substring(index + 1);
    }
  }

  /**
   * Get the method name
   *
   * @return the method name as String
   */
  public String getMethodName() {
    return this.methodInfo == null ? null : this.methodInfo.getName();
  }

  /**
   * Get the method signature
   *
   * @return the method signature as String
   */
  public String getMethodSignature() {
    return this.methodInfo == null ? null : this.methodInfo.getSignature();
  }

  /**
   * Get the MethodGen object linked to the method info
   *
   * @return null if saved method info is null, a MethodGen object if there is MethodGen linked to the info object
   */
  public MethodGen getMethodGen() {
    if (methodInfo == null) {
      return null;
    }
    if (this.lazyMethodGen == null) {
      this.lazyMethodGen =
          new MethodGen(methodInfo, classInfo.getClassName(), classInfo.getConstantPool());
    }
    return this.lazyMethodGen;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.classInfo, this.methodInfo);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof ClassMethodInfo) {
      final ClassMethodInfo info = (ClassMethodInfo) obj;
      return this.classInfo.equals(info.classInfo) && this.methodInfo.equals(info.methodInfo);
    }
    return false;
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    if (this.classInfo != null) {
      result.append(this.classInfo.getClassName());
    }
    if (methodInfo != null) {
      result.append('#').append(this.methodInfo.getName()).append(' ')
          .append(this.methodInfo.getSignature());
    }
    return result.toString();
  }

  /**
   * Check that the method is a native one
   *
   * @return returns true if the method is a native one
   */
  public boolean isNative() {
    return this.methodInfo != null && this.methodInfo.isNative();
  }
}
