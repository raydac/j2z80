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

package com.igormaznitsa.j2z80.ids;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.junit.Test;

public class ClassMethodInfoTest {

  private static final ClassGen CLASS_MOCK = mock(ClassGen.class);
  private static final ClassGen CLASS_MOCK_NO_PACKAGE = mock(ClassGen.class);
  private static final ConstantPool CP_MOCK = new ConstantPool(new Constant[] {
      new ConstantUtf8("zero"),
      new ConstantUtf8("someMethod"),
      new ConstantUtf8("(III)V"),
  });
  private static final Method METHOD_MOCK =
      new Method(Const.ACC_PUBLIC, 1, 2, new Attribute[0], CP_MOCK);

  static {
    when(CLASS_MOCK.getClassName()).thenReturn("com.package.klazz");
    when(CLASS_MOCK_NO_PACKAGE.getClassName()).thenReturn("klazz");
  }

  @Test
  public void testGetPackageName() {
    assertEquals("com.package", new ClassMethodInfo(CLASS_MOCK, METHOD_MOCK).getPackageName());
  }

  @Test
  public void testGetPackageName_RootPackage() {
    assertEquals("", new ClassMethodInfo(CLASS_MOCK_NO_PACKAGE, METHOD_MOCK).getPackageName());
  }

  @Test
  public void testGetClassName() {
    assertEquals("klazz", new ClassMethodInfo(CLASS_MOCK, METHOD_MOCK).getOnlyClassName());
  }

  @Test
  public void testGetMethodName() {
    assertEquals("someMethod", new ClassMethodInfo(CLASS_MOCK, METHOD_MOCK).getMethodName());
  }

  @Test
  public void testGetMethodSignature() {
    assertEquals("(III)V", new ClassMethodInfo(CLASS_MOCK, METHOD_MOCK).getMethodSignature());
  }
}
