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

import static java.util.Arrays.deepEquals;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 * Immutable identity for a Java method that is being processed by the translator. A method ID
 * combines the owning class, method name, argument signature, and return type to form a stable
 * key that can be used across the translator's class registry, runtime dispatch tables, and
 * generated assembly labels.
 *
 * <p>Method comparisons are based on the full JVM signature, so overloaded methods are treated as
 * distinct identities.</p>
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MethodID {
  private final String methodId;
  private final String methodLabel;
  private final String className;
  private final String methodName;
  private final Type returnType;
  private final Type[] argTypes;
  private final ClassID classId;

  /**
   * Creates a method identity from a BCEL method definition.
   *
   * @param methodGen the method definition to wrap, must not be {@code null}
   */
  public MethodID(final MethodGen methodGen) {
    this(methodGen.getClassName(), methodGen.getName(), methodGen.getReturnType(),
        methodGen.getArgumentTypes());
  }

  /**
   * Creates a method identity from a class and method definition pair.
   *
   * @param c the class that owns the method, must not be {@code null}
   * @param m the method definition, must not be {@code null}
   */
  public MethodID(final ClassGen c, final Method m) {
    this(c.getClassName(), m);
  }

  /**
   * Creates a method identity from a class name and a BCEL method definition.
   *
   * @param className the owning class name, must not be {@code null}
   * @param method the method definition, must not be {@code null}
   */
  public MethodID(final String className, final Method method) {
    this(className, method.getName(), method.getReturnType(), method.getArgumentTypes());
  }

  /**
   * Creates a method identity from explicit method metadata.
   *
   * @param className the owning class name, must not be {@code null}
   * @param methodName the method name, must not be {@code null}
   * @param returnType the return type descriptor, must not be {@code null}
   * @param argTypes the argument type descriptors, must not be {@code null}
   */
  public MethodID(final String className, final String methodName, final Type returnType,
                  final Type[] argTypes) {
    requireNonNull(className, "ClassName must not be null");
    requireNonNull(methodName, "Method name must not be null");
    requireNonNull(returnType, "ReturnType must not be null");
    requireNonNull(argTypes, "ArgTypes must not be null");
    this.methodId =
        className + '.' + methodName + '.' + Type.getMethodSignature(returnType, argTypes);
    this.methodLabel =
        LabelAndFrameUtils.makeLabelNameForMethod(className, methodName, returnType, argTypes);
    this.className = className;
    this.methodName = methodName;
    this.returnType = returnType;
    this.argTypes = argTypes;
    this.classId = new ClassID(className);
  }

  /**
   * Returns the class identity for the owner of this method.
   *
   * @return the owning class identity
   */
  public ClassID getClassID() {
    return this.classId;
  }

  /**
   * Returns the canonical class name that owns the method.
   *
   * @return the owning class name
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Returns the method name.
   *
   * @return the method name
   */
  public String getMethodName() {
    return this.methodName;
  }

  /**
   * Returns the method return type.
   *
   * @return the method return descriptor
   */
  public Type getReturnType() {
    return this.returnType;
  }

  /**
   * Returns the argument type list for this method.
   *
   * @return the method argument descriptors in declaration order
   */
  public Type[] getArgs() {
    return this.argTypes;
  }

  @Override
  public int hashCode() {
    return this.methodId.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (obj instanceof MethodID that) {
      return this.methodId.equals(that.methodId);
    }
    return false;
  }

  /**
   * Returns the generated assembly label used for this method in the final output.
   *
   * @return the generated method label
   */
  public String getMethodLabel() {
    return this.methodLabel;
  }

  @Override
  public String toString() {
    return this.methodId + "(" + this.methodLabel + ")";
  }

  /**
   * Looks for a method with the same signature in the supplied class definition.
   *
   * @param cgen the class definition to search, must not be {@code null}
   * @return the matching BCEL method, or {@code null} if no compatible method exists
   */
  public Method findCompatibleMethod(final ClassGen cgen) {
    requireNonNull(cgen, "Class must not be null");
    for (final Method m : cgen.getMethods()) {
      if (
          this.methodName.equals(m.getName())
              && deepEquals(this.argTypes, m.getArgumentTypes())
              && this.returnType.equals(m.getReturnType())
      ) {
        return m;
      }
    }
    return null;
  }
}
