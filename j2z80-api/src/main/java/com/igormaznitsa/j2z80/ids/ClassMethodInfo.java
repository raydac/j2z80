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

package com.igormaznitsa.j2z80.ids;

import java.util.Objects;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * A compact description of a method as it appears within a translated Java class. This value object
 * keeps together the owning {@link ClassGen}, the original BCEL {@link Method}, and any optional
 * lazily generated {@link MethodGen} representation used by the translator during code generation.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ClassMethodInfo {

  private final ClassGen classInfo;
  private final Method methodInfo;
  private final int id;
  private MethodGen lazyMethodGen;

  /**
   * Creates a method description with an explicit runtime identifier.
   *
   * @param classInfo the class that contains the method, must not be {@code null}
   * @param methodInfo the underlying BCEL method definition, must not be {@code null}
   * @param id the runtime identifier assigned to this method
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final int id) {
    this.classInfo = classInfo;
    this.methodInfo = methodInfo;
    this.id = id;
  }

  /**
   * Creates a method description without a runtime identifier.
   *
   * @param classInfo the class that contains the method, must not be {@code null}
   * @param methodInfo the underlying BCEL method definition, must not be {@code null}
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo) {
    this(classInfo, methodInfo, -1);
  }

  /**
   * Creates a method description and binds a lazily created method generator for later use.
   *
   * @param classInfo the class that contains the method, must not be {@code null}
   * @param methodInfo the underlying BCEL method definition, must not be {@code null}
   * @param methodGen the method generator to reuse, or {@code null} if one should be created on demand
   */
  public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo,
                         final MethodGen methodGen) {
    this(classInfo, methodInfo, -1);
    this.lazyMethodGen = methodGen;
  }

  /**
   * Returns the numeric runtime identifier assigned to this method.
   *
   * @return the method identifier, or {@code -1} when no identifier has been assigned yet
   */
  public int getUID() {
    return id;
  }

  /**
   * Returns the class metadata associated with this method.
   *
   * @return the owner class definition
   */
  public ClassGen getClassInfo() {
    return this.classInfo;
  }

  /**
   * Returns the original BCEL method definition.
   *
   * @return the method description from the class file
   */
  public Method getMethodInfo() {
    return this.methodInfo;
  }

  /**
   * Returns the package name portion of the owning class.
   *
   * @return the package name, or an empty string when the class is in the default package
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
   * Returns the canonical class name.
   *
   * @return the fully qualified owner class name
   */
  public String getCanonicalClassName() {
    return this.classInfo.getClassName();
  }

  /**
   * Returns the simple class name without the package prefix.
   *
   * @return the unqualified class name
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
   * Returns the method name.
   *
   * @return the method name, or {@code null} when the underlying definition is unavailable
   */
  public String getMethodName() {
    return this.methodInfo == null ? null : this.methodInfo.getName();
  }

  /**
   * Returns the method signature in the JVM descriptor format.
   *
   * @return the method signature, or {@code null} when the underlying definition is unavailable
   */
  public String getMethodSignature() {
    return this.methodInfo == null ? null : this.methodInfo.getSignature();
  }

  /**
   * Returns the lazily initialized method generator for this method.
   *
   * @return the cached {@link MethodGen} instance, or {@code null} when the original method is
   *         absent
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
    if (obj instanceof ClassMethodInfo info) {
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
   * Determines whether the underlying method is declared as native.
   *
   * @return {@code true} if the method is native, otherwise {@code false}
   */
  public boolean isNative() {
    return this.methodInfo != null && this.methodInfo.isNative();
  }
}
