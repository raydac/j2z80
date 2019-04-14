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

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMULTIANEWARRAY extends AbstractTestBasedOnMemoryManager {

  @Test(timeout = 3000L)
  public void testCreateSingleArray() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new MULTIANEWARRAY(0, (short) 1)}, calculateMemorySize(1000));
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testCreateTwoDimensionSingleElementArrays() throws Exception {
    final int dim1 = 1;
    final int dim2 = 1;

    push(dim1);
    push(dim2);
    assertAllocateCommand(new Instruction[] {new MULTIANEWARRAY(0, (short) 2)}, calculateMemorySize(dim1, dim2));
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();

    // check that addresses are not null
    assertEquals(dim1, peekw(getInitialMemoryAddress() + 1));
    for (int i = 0; i < dim1; i++) {
      final int arrayAddress = peekw(getInitialMemoryAddress() + 3 + (i * 2));
      assertEquals(dim2, peekw(arrayAddress - 2));
    }
  }

  @Test(timeout = 3000L)
  public void testCreateTwoDimensionNoSingleElementArrays() throws Exception {
    final int dim1 = 233;
    final int dim2 = 20;

    push(dim1);
    push(dim2);
    assertAllocateCommand(new Instruction[] {new MULTIANEWARRAY(0, (short) 2)}, calculateMemorySize(dim1, dim2));
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();

    // check that addresses are not null
    assertEquals(dim1, peekw(getInitialMemoryAddress() + 1));
    for (int i = 0; i < dim1; i++) {
      final int arrayAddress = peekw(getInitialMemoryAddress() + 3 + (i * 2));
      assertEquals(dim2, peekw(arrayAddress - 2));
    }

  }

  @Test(timeout = 3000L)
  public void testCreateThreeDimensionNoSingleElementArrays() throws Exception {
    final int dim1 = 4;
    final int dim2 = 11;
    final int dim3 = 23;

    push(dim1);
    push(dim2);
    push(dim3);

    assertAllocateCommand(new Instruction[] {new MULTIANEWARRAY(0, (short) 3)}, calculateMemorySize(dim1, dim2, dim3));
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testCreateFourDimensionNoSingleElementArrays() throws Exception {
    final int dim1 = 2;
    final int dim2 = 3;
    final int dim3 = 5;
    final int dim4 = 8;

    push(dim1);
    push(dim2);
    push(dim3);
    push(dim4);
    assertAllocateCommand(new Instruction[] {new MULTIANEWARRAY(0, (short) 4)}, calculateMemorySize(dim1, dim2, dim3, dim4));
    assertEquals(getInitialMemoryAddress() + 3, pop());
    assertStackEmpty();
  }

  @Test
  public void testCalculateMemorySize() {
    assertEquals(31, calculateMemorySize(2, 1, 2));
  }

  private int calculateMemorySize(final int... dimensions) {
    int result = 0;

    int prev = 1;

    for (final int i : dimensions) {
      result += prev * (i * 2 + 3);
      prev *= i;
    }

    return result;
  }
}
