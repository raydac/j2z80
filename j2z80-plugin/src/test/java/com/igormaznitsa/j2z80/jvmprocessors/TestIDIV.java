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

import org.apache.bcel.generic.IDIV;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestIDIV extends AbstractIntMathManagerBasedTest {

  @Test(timeout = 3000L)
  public void testBothPositive() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = 12344;
    final int val2 = 2342;

    push(val1);
    push(val2);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (val1 / val2), (short) (pop()));
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testBothNegative() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = -6723;
    final int val2 = -21;

    push(val1);
    push(val2);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (val1 / val2), (short) (pop()));
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testFirstNegative() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = -6723;
    final int val2 = 21;

    push(val1);
    push(val2);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (val1 / val2), (short) (pop()));
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testSecondNegative() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = 6723;
    final int val2 = -21;

    push(val1);
    push(val2);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (val1 / val2), (short) (pop()));
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testFirstZero() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = 0;
    final int val2 = 11;

    push(val1);
    push(val2);

    assertLinearExecutionToEnd(asm);
    assertEquals(0, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testSecondZero() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = 22;
    final int val2 = 0;

    push(val1);
    push(val2);

    assertException(asm);
  }

  @Test(timeout = 3000L)
  public void testBothZero() throws Exception {
    final String asm = prepareForTest(new IDIV());

    final int val1 = 0;
    final int val2 = 0;

    push(val1);
    push(val2);

    assertException(asm);
  }

}
