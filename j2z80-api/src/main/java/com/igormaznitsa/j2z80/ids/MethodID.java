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

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static java.util.Arrays.deepEquals;

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 * The Class describes an identifier for a method
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
   * A Constructor
   *
   * @param methodGen a MethodGen object, must not be null
   */
  public MethodID(final MethodGen methodGen) {
    this(methodGen.getClassName(), methodGen.getName(), methodGen.getReturnType(),
        methodGen.getArgumentTypes());
  }

  /**
   * A Constructor
   *
   * @param c the ClassGen owns the method, must not be null
   * @param m the method, must not be null
   */
  public MethodID(final ClassGen c, final Method m) {
    this(c.getClassName(), m);
  }

  /**
   * A Constructor
   *
   * @param className the class name, must not be null
   * @param method    the method, must not be null
   */
  public MethodID(final String className, final Method method) {
    this(className, method.getName(), method.getReturnType(), method.getArgumentTypes());
  }

  /**
   * A Constructor
   *
   * @param className  the class name, must not be null
   * @param methodName the method name, must not be null
   * @param returnType the return type signature for the method, must not be null
   * @param argTypes   the argument type signatures for the method, must not be null
   */
  public MethodID(final String className, final String methodName, final Type returnType, final Type[] argTypes) {
    assertNotNull("ClassName must not contain null", className);
    assertNotNull("Method name must not be null", methodName);
    assertNotNull("ReturnType must not be null", returnType);
    assertNotNull("ArgTypes must not be null", argTypes);
    this.methodId = className + '.' + methodName + '.' + Type.getMethodSignature(returnType, argTypes);
    this.methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(className, methodName, returnType, argTypes);
    this.className = className;
    this.methodName = methodName;
    this.returnType = returnType;
    this.argTypes = argTypes;
    this.classId = new ClassID(className);
  }

  /**
   * Get the class Id for the class owns the method
   *
   * @return the class id object
   */
  public ClassID getClassID() {
    return this.classId;
  }

  /**
   * Get the class name of the class owns the menthod
   *
   * @return the class name
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Get the method name
   *
   * @return the method name
   */
  public String getMethodName() {
    return this.methodName;
  }

  /**
   * Get the return type signature for the method
   *
   * @return the return type
   */
  public Type getReturnType() {
    return this.returnType;
  }

  /**
   * Get the argument type signatures for the method
   *
   * @return the argument types for the method
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

    if (obj instanceof MethodID) {
      final MethodID that = (MethodID) obj;
      return this.methodId.equals(that.methodId);
    }
    return false;
  }

  /**
   * Get the method label
   *
   * @return the method label as String
   */
  public String getMethodLabel() {
    return this.methodLabel;
  }

  @Override
  public String toString() {
    return this.methodId + "(" + this.methodLabel + ")";
  }

  /**
   * Find compatible method inside a class
   *
   * @param cgen a class object where the compatible method will be looked for
   * @return a found compatible method if it is found or null if not found
   */
  public Method findCompatibleMethod(final ClassGen cgen) {
    assertNotNull("Class must not be null", cgen);
    for (final Method m : cgen.getMethods()) {
      if (this.methodName.equals(m.getName()) && deepEquals(this.argTypes, m.getArgumentTypes()) &&
          this.returnType.equals(m.getReturnType())) {
        return m;
      }
    }
    return null;
  }
}
