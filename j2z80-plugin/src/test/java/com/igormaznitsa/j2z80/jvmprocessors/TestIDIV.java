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
