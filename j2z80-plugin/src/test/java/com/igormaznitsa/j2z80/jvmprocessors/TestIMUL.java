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

import org.apache.bcel.generic.IMUL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestIMUL extends AbstractIntMathManagerBasedTest {

  @Test(timeout = 3000L)
  public void testBothZero() throws Exception {
    final String asm = prepareForTest(new IMUL());

    push(0);
    push(0);

    assertLinearExecutionToEnd(asm);
    assertEquals(0, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTheFirstZero() throws Exception {
    final String asm = prepareForTest(new IMUL());

    push(0);
    push(3452);

    assertLinearExecutionToEnd(asm);
    assertEquals(0, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTheSecondZero() throws Exception {
    final String asm = prepareForTest(new IMUL());

    push(2234);
    push(0);

    assertLinearExecutionToEnd(asm);
    assertEquals(0, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testBothPositive() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = 234;
    final int SECOND = 123;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testBothNegative() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = -223;
    final int SECOND = -58;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testFirstPositive() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = 22;
    final int SECOND = -90;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testSecondPositive() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = -721;
    final int SECOND = -23;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testOverflowPositive() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = 12332;
    final int SECOND = 9223;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testOverflowFirstNegative() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = -12332;
    final int SECOND = 8234;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testOverflowSecondNegative() throws Exception {
    final String asm = prepareForTest(new IMUL());

    final int FIRST = 12332;
    final int SECOND = -8234;

    push(FIRST);
    push(SECOND);

    assertLinearExecutionToEnd(asm);
    assertEquals((short) (FIRST * SECOND), (short) pop());
    assertStackEmpty();
  }
}
