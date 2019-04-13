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
