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

package com.igormaznitsa.j2z80.translator.jar;

import static org.apache.bcel.Const.ACC_PUBLIC;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.junit.Test;

public class ClassValidatorTest {

  @Test
  public void acceptsBoundaryIntegerAndLongValues() {
    final ClassGen classGen = this.newClass();
    final ConstantPoolGen constantPool = classGen.getConstantPool();
    constantPool.addInteger(Short.MIN_VALUE);
    constantPool.addInteger(Short.MAX_VALUE);
    constantPool.addLong(Integer.MIN_VALUE);
    constantPool.addLong(Integer.MAX_VALUE);
    constantPool.addUtf8("x".repeat(255));

    assertNull(ClassValidator.validateClass(classGen));
  }

  @Test
  public void rejectsIntegerOutsideSigned16BitRange() {
    assertTrue(this.validateInteger(Short.MIN_VALUE - 1).startsWith("Integer value"));
    assertTrue(this.validateInteger(Short.MAX_VALUE + 1).startsWith("Integer value"));
  }

  @Test
  public void rejectsLongOutsideSigned32BitRange() {
    assertTrue(this.validateLong((long) Integer.MIN_VALUE - 1L).startsWith("Long value"));
    assertTrue(this.validateLong((long) Integer.MAX_VALUE + 1L).startsWith("Long value"));
  }

  @Test
  public void rejectsUnsupportedStringEncodingsAndLengths() {
    final ClassGen nonAsciiClass = this.newClass();
    nonAsciiClass.getConstantPool().addUtf8("bad\u0100text");
    assertNotNull(ClassValidator.validateClass(nonAsciiClass));

    final ClassGen longStringClass = this.newClass();
    longStringClass.getConstantPool().addUtf8("x".repeat(256));
    assertTrue(ClassValidator.validateClass(longStringClass).startsWith("Too long string"));
  }

  private String validateInteger(final int value) {
    final ClassGen classGen = this.newClass();
    classGen.getConstantPool().addInteger(value);
    return ClassValidator.validateClass(classGen);
  }

  private String validateLong(final long value) {
    final ClassGen classGen = this.newClass();
    classGen.getConstantPool().addLong(value);
    return ClassValidator.validateClass(classGen);
  }

  private ClassGen newClass() {
    return new ClassGen("demo.Validation", "java.lang.Object", "Validation.java", ACC_PUBLIC,
        null);
  }
}
