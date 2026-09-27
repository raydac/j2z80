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

package com.igormaznitsa.j2z80.jvmprocessors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.j2z80.translator.utils.HalfFloat;
import org.apache.bcel.generic.F2I;
import org.apache.bcel.generic.FADD;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FDIV;
import org.apache.bcel.generic.FMUL;
import org.apache.bcel.generic.FNEG;
import org.apache.bcel.generic.FREM;
import org.apache.bcel.generic.FSUB;
import org.apache.bcel.generic.I2F;
import org.apache.bcel.generic.Instruction;
import org.junit.Test;

public class TestFloatArithmetic extends AbstractFloatMathTest {

  private static final float[] SAMPLES = {
      -8f, -2f, -1f, -0.5f, 0f, 0.5f, 1f, 1.5f, 2f, 3f, 4f, 10f
  };

  @Test(timeout = 10000L)
  public void addsOneAndOne() throws Exception {
    this.assertBinary(new FADD(), 1f, 1f, Float::sum);
  }

  @Test(timeout = 180000L)
  public void matchesFloat32RoundingOnSamples() throws Exception {
    for (final float left : SAMPLES) {
      for (final float right : SAMPLES) {
        this.assertBinary(new FADD(), left, right, Float::sum);
        this.assertBinary(new FSUB(), left, right, (a, b) -> a - b);
        this.assertBinary(new FMUL(), left, right, (a, b) -> a * b);
        if (right != 0f) {
          this.assertBinary(new FDIV(), left, right, (a, b) -> a / b);
        }
      }
    }
  }

  @Test(timeout = 10000L)
  public void handlesSignedZeroInfAndNan() throws Exception {
    assertEquals(0x0000, this.runBinary(new FADD(), 0x0000, 0x8000));
    assertEquals(0x8000, this.runBinary(new FADD(), 0x8000, 0x8000));
    assertEquals(0x7C00, this.runBinary(new FADD(), 0x7C00, HalfFloat.toBits(1f)));
    assertTrue(HalfFloat.isNaN(this.runBinary(new FADD(), 0x7C00, 0xFC00)));
    assertTrue(HalfFloat.isNaN(this.runBinary(new FADD(), HalfFloat.NAN, HalfFloat.toBits(1f))));
    assertEquals(0xFC00, this.runBinary(new FDIV(), HalfFloat.toBits(1f), 0x8000));
    assertTrue(HalfFloat.isNaN(this.runBinary(new FDIV(), 0x0000, 0x0000)));
    assertTrue(HalfFloat.isNaN(this.runBinary(new FMUL(), 0x7C00, 0x0000)));
    assertEquals(0xBC00, this.runUnary(new FNEG(), HalfFloat.toBits(1f)));
    assertEquals(0x0000, this.runUnary(new FNEG(), 0x8000));
  }

  @Test(timeout = 10000L)
  public void remainderMatchesCommonCases() throws Exception {
    this.assertBinary(new FREM(), 5f, 2f, (a, b) -> (float) Math.IEEEremainder(a, b));
    this.assertBinary(new FREM(), 5.5f, 2f, (a, b) -> (float) Math.IEEEremainder(a, b));
    this.assertBinary(new FREM(), 7.5f, 2.5f, (a, b) -> (float) Math.IEEEremainder(a, b));
    assertTrue(HalfFloat.isNaN(this.runBinary(new FREM(), HalfFloat.toBits(1f), 0)));
    assertEquals(HalfFloat.toBits(1f), this.runBinary(new FREM(), HalfFloat.toBits(1f), 0x7C00));
  }

  @Test(timeout = 10000L)
  public void convertsBetweenIntAndFloat() throws Exception {
    assertEquals(HalfFloat.toBits(0f), this.runUnary(new I2F(), 0));
    assertEquals(HalfFloat.toBits(1f), this.runUnary(new I2F(), 1));
    assertEquals(HalfFloat.toBits(-2f), this.runUnary(new I2F(), -2));
    assertEquals(HalfFloat.toBits(3f), this.runUnary(new I2F(), 3));
    assertEquals(0xF800, this.runUnary(new I2F(), -32768));
    assertEquals(1, (short) this.runUnary(new F2I(), HalfFloat.toBits(1.9f)));
    assertEquals(-1, (short) this.runUnary(new F2I(), HalfFloat.toBits(-1.9f)));
    assertEquals(0, (short) this.runUnary(new F2I(), HalfFloat.NAN));
    assertEquals(32767, (short) this.runUnary(new F2I(), HalfFloat.POSITIVE_INFINITY));
    assertEquals(-32768, (short) this.runUnary(new F2I(), HalfFloat.NEGATIVE_INFINITY));
  }

  @Test(timeout = 10000L)
  public void comparesWithNanPolarity() throws Exception {
    assertEquals(-1,
        (short) this.runBinary(new FCMPL(), HalfFloat.toBits(1f), HalfFloat.toBits(2f)));
    assertEquals(1,
        (short) this.runBinary(new FCMPG(), HalfFloat.toBits(2f), HalfFloat.toBits(1f)));
    assertEquals(0,
        (short) this.runBinary(new FCMPL(), HalfFloat.toBits(1f), HalfFloat.toBits(1f)));
    assertEquals(0, (short) this.runBinary(new FCMPL(), 0x0000, 0x8000));
    assertEquals(-1, (short) this.runBinary(new FCMPL(), HalfFloat.NAN, HalfFloat.toBits(1f)));
    assertEquals(1, (short) this.runBinary(new FCMPG(), HalfFloat.NAN, HalfFloat.toBits(1f)));
    assertEquals(1,
        (short) this.runBinary(new FCMPL(), HalfFloat.POSITIVE_INFINITY, HalfFloat.toBits(1f)));
    assertEquals(-1,
        (short) this.runBinary(new FCMPL(), HalfFloat.NEGATIVE_INFINITY, HalfFloat.toBits(-1f)));
  }

  private void assertBinary(final Instruction instruction, final float left, final float right,
                            final FloatOp operation) throws Exception {
    final int leftBits = HalfFloat.toBits(left);
    final int rightBits = HalfFloat.toBits(right);
    final int actual = this.runBinary(instruction, leftBits, rightBits);
    final float exact = operation.apply(HalfFloat.toFloat(leftBits), HalfFloat.toFloat(rightBits));
    final int expected = HalfFloat.toBits(exact);
    if (HalfFloat.isNaN(expected)) {
      assertTrue(instruction.getName() + " " + left + " , " + right, HalfFloat.isNaN(actual));
      return;
    }
    assertEquals(instruction.getName() + " " + left + " , " + right
            + " expected " + Integer.toHexString(expected) + " actual " + Integer.toHexString(actual),
        expected, actual);
  }

  @FunctionalInterface
  private interface FloatOp {
    float apply(float left, float right);
  }
}
