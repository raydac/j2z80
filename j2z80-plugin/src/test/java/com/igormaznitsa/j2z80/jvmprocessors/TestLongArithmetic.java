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

import com.igormaznitsa.j2z80.translator.utils.HalfFloat;
import org.apache.bcel.generic.F2L;
import org.apache.bcel.generic.I2L;
import org.apache.bcel.generic.L2F;
import org.apache.bcel.generic.L2I;
import org.apache.bcel.generic.LADD;
import org.apache.bcel.generic.LAND;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LDIV;
import org.apache.bcel.generic.LMUL;
import org.apache.bcel.generic.LNEG;
import org.apache.bcel.generic.LOR;
import org.apache.bcel.generic.LREM;
import org.apache.bcel.generic.LSHL;
import org.apache.bcel.generic.LSHR;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LUSHR;
import org.apache.bcel.generic.LXOR;
import org.junit.Test;

public class TestLongArithmetic extends AbstractLongMathTest {

  @Test(timeout = 10000L)
  public void arithmeticMatches32BitInt() throws Exception {
    assertEquals(3, this.runBinary(new LADD(), 1, 2));
    assertEquals(Integer.MIN_VALUE, this.runBinary(new LADD(), Integer.MAX_VALUE, 1));
    assertEquals(0, this.runBinary(new LSUB(), 5, 5));
    assertEquals(-8, this.runBinary(new LSUB(), -3, 5));
    assertEquals(42, this.runBinary(new LMUL(), 6, 7));
    assertEquals(300000, this.runBinary(new LMUL(), 100000, 3));
    assertEquals(-6, this.runBinary(new LMUL(), -2, 3));
    assertEquals(Integer.MIN_VALUE, this.runBinary(new LMUL(), Integer.MIN_VALUE, 1));
    assertEquals(3, this.runBinary(new LDIV(), 7, 2));
    assertEquals(-3, this.runBinary(new LDIV(), -7, 2));
    assertEquals(-3, this.runBinary(new LDIV(), 7, -2));
    assertEquals(3, this.runBinary(new LDIV(), -7, -2));
    assertEquals(Integer.MIN_VALUE, this.runBinary(new LDIV(), Integer.MIN_VALUE, -1));
    assertEquals(1, this.runBinary(new LDIV(), Integer.MIN_VALUE, Integer.MIN_VALUE));
    assertEquals(1, this.runBinary(new LREM(), 7, 2));
    assertEquals(-1, this.runBinary(new LREM(), -7, 2));
    assertEquals(1, this.runBinary(new LREM(), 7, -2));
    assertEquals(-5, this.runUnary(new LNEG(), 5));
    assertEquals(Integer.MIN_VALUE, this.runUnary(new LNEG(), Integer.MIN_VALUE));
    assertEquals(0x00FF, this.runBinary(new LAND(), 0x0FFF, 0x00FF));
    assertEquals(0x0FFF, this.runBinary(new LOR(), 0x0F0F, 0x00F0));
    assertEquals(0x0FF0, this.runBinary(new LXOR(), 0x0FFF, 0x000F));
  }

  @Test(timeout = 10000L)
  public void shiftsUseLowSixBits() throws Exception {
    assertEquals(65536, this.runShift(new LSHL(), 1, 16));
    assertEquals(Integer.MIN_VALUE, this.runShift(new LSHL(), 1, 31));
    assertEquals(0, this.runShift(new LSHL(), 1, 32));
    assertEquals(0, this.runShift(new LSHL(), 1, 63));
    assertEquals(-4, this.runShift(new LSHR(), -8, 1));
    assertEquals(-1, this.runShift(new LSHR(), -1, 40));
    assertEquals(0x7FFFFFFC, this.runShift(new LUSHR(), -8, 1));
    assertEquals(0, this.runShift(new LUSHR(), -1, 32));
  }

  @Test(timeout = 10000L)
  public void comparesAndConverts() throws Exception {
    assertEquals(-1, this.runCompare(-1, 1));
    assertEquals(0, this.runCompare(5, 5));
    assertEquals(1, this.runCompare(2, -2));
    assertEquals(-1, this.runCompare(Integer.MIN_VALUE, -1));

    this.beforeTest();
    this.push(-2);
    this.assertLinearExecutionToEnd(this.translate(new I2L()));
    assertEquals(-2, this.popLong());

    this.beforeTest();
    this.pushLong(0x12345678);
    this.assertLinearExecutionToEnd(this.translate(new L2I()));
    assertEquals(0x5678, this.pop() & 0xFFFF);

    this.beforeTest();
    this.pushLong(3);
    this.assertLinearExecutionToEnd(this.translate(new L2F()));
    assertEquals(HalfFloat.toBits(3f), this.pop() & 0xFFFF);

    this.beforeTest();
    this.pushLong(65536);
    this.assertLinearExecutionToEnd(this.translate(new L2F()));
    assertEquals(HalfFloat.POSITIVE_INFINITY, this.pop() & 0xFFFF);

    this.beforeTest();
    this.push(HalfFloat.toBits(1.9f));
    this.assertLinearExecutionToEnd(this.translate(new F2L()));
    assertEquals(1, this.popLong());

    this.beforeTest();
    this.push(HalfFloat.NEGATIVE_INFINITY);
    this.assertLinearExecutionToEnd(this.translate(new F2L()));
    assertEquals(Integer.MIN_VALUE, this.popLong());
  }

  private int runCompare(final int left, final int right) throws Exception {
    this.beforeTest();
    this.pushLong(left);
    this.pushLong(right);
    this.assertLinearExecutionToEnd(this.translate(new LCMP()));
    final int result = (short) this.pop();
    this.assertStackEmpty();
    return result;
  }

  private int runShift(final org.apache.bcel.generic.Instruction instruction, final int value,
                       final int count) throws Exception {
    this.beforeTest();
    this.pushLong(value);
    this.push(count);
    this.assertLinearExecutionToEnd(this.translate(instruction));
    final int result = this.popLong();
    this.assertStackEmpty();
    return result;
  }
}
