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

import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.NEWARRAY;
import org.junit.Test;

public class TestNEWARRAY extends AbstractTestBasedOnMemoryManager {

  private boolean poisonHeap;

  @Override
  protected void beforeExec(final int heapStart) {
    if (!this.poisonHeap) {
      return;
    }
    for (int offset = 0; offset < 16; offset++) {
      this.pokeb(heapStart + offset, 0xFF);
    }
  }

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

  @Test(timeout = 3000L)
  public void testArrayCreation_LongArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.LONG)}, 4003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_DoubleArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.DOUBLE)}, 4003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testByteArrayPayloadIsCleared() throws Exception {
    this.poisonHeap = true;
    push(4);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.BYTE)}, 7);
    final int address = pop();
    assertEquals(1, peekb(address - 3));
    for (int offset = 0; offset < 4; offset++) {
      assertEquals(0, peekb(address + offset));
    }
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testWordArraySizeKeepsCarryIntoHighByte() throws Exception {
    assertLinearExecutionToEnd(
        "LD BC,128\nCALL " + SUB_ALLOCATE_WORDARRAY + "\nCALL " + SUB_GET_ARRAY_SIZE +
            "\nPUSH BC\n",
        3 + 256);
    assertEquals(256, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testByteArraySizeIsNotDoubled() throws Exception {
    assertLinearExecutionToEnd(
        "LD BC,200\nCALL " + SUB_ALLOCATE_BYTEARRAY + "\nCALL " + SUB_GET_ARRAY_SIZE +
            "\nPUSH BC\n",
        3 + 200);
    assertEquals(200, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testFreeMemoryIsCallerStackMinusHeapTop() throws Exception {
    assertLinearExecutionToEnd("CALL " + SUB_GETFREEMEMORY + "\nPUSH BC\n", 0);
    assertEquals(INIT_SP - this.getInitialMemoryAddress(), pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testArrayCreation_FloatArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new NEWARRAY(BasicType.FLOAT)}, 2003);
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }
}
