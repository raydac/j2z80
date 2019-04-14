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
package com.igormaznitsa.j2z80.jvmprocessors;

import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.NEWARRAY;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNEWARRAY extends AbstractTestBasedOnMemoryManager {

  @Test(timeout = 3000L)
  public void testArrayCreation_BooleanArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.BOOLEAN)}, 1003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_ByteArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.BYTE)}, 1003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_CharArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.CHAR)}, 1003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_ShortArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.SHORT)}, 2003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_IntArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.INT)}, 2003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayCreation_LongArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.LONG)}, 2003);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayCreation_DoubleArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.DOUBLE)}, 2003);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayCreation_FloatArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.FLOAT)}, 2003);
  }
}
