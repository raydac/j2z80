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
import org.apache.bcel.generic.D2F;
import org.apache.bcel.generic.D2I;
import org.apache.bcel.generic.D2L;
import org.apache.bcel.generic.DADD;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DDIV;
import org.apache.bcel.generic.DMUL;
import org.apache.bcel.generic.DNEG;
import org.apache.bcel.generic.DSUB;
import org.apache.bcel.generic.F2D;
import org.apache.bcel.generic.I2D;
import org.junit.Test;

public class TestDoubleArithmetic extends AbstractDoubleMathTest {

  @Test(timeout = 20000L)
  public void addsOneAndOne() throws Exception {
    assertEquals(Float.floatToRawIntBits(2f), this.runBinary(new DADD(), 1f, 1f));
  }

  @Test(timeout = 20000L)
  public void arithmeticMatchesBinary32() throws Exception {
    this.assertFloat(new DADD(), 1f, 1f, Float::sum);
    this.assertFloat(new DSUB(), 5f, 2f, (a, b) -> a - b);
    this.assertFloat(new DMUL(), 2f, 3f, (a, b) -> a * b);
    this.assertFloat(new DDIV(), 10f, 4f, (a, b) -> a / b);
    this.assertFloat(new DDIV(), 1f, 1f, (a, b) -> a / b);
    this.assertFloat(new DMUL(), -2f, 3f, (a, b) -> a * b);
  }

  @Test(timeout = 20000L)
  public void negatesAndConverts() throws Exception {
    this.beforeTest();
    this.pushDouble(Float.floatToRawIntBits(1f));
    this.assertLinearExecutionToEnd(this.translate(new DNEG()));
    assertEquals(Float.floatToRawIntBits(-1f), this.popDouble());

    this.beforeTest();
    this.push(1);
    this.assertLinearExecutionToEnd(this.translate(new I2D()));
    assertEquals(Float.floatToRawIntBits(1f), this.popDouble());

    this.beforeTest();
    this.pushDouble(Float.floatToRawIntBits(1.9f));
    this.assertLinearExecutionToEnd(this.translate(new D2I()));
    assertEquals(1, (short) this.pop());

    this.beforeTest();
    this.pushDouble(Float.floatToRawIntBits(3f));
    this.assertLinearExecutionToEnd(this.translate(new D2L()));
    final int low = this.pop() & 0xFFFF;
    final int high = this.pop() & 0xFFFF;
    assertEquals(3, (high << 16) | low);

    this.beforeTest();
    this.push(HalfFloat.toBits(1f));
    this.assertLinearExecutionToEnd(this.translate(new F2D()));
    assertEquals(Float.floatToRawIntBits(1f), this.popDouble());

    this.beforeTest();
    this.pushDouble(Float.floatToRawIntBits(2f));
    this.assertLinearExecutionToEnd(this.translate(new D2F()));
    assertEquals(HalfFloat.toBits(2f), this.pop() & 0xFFFF);
  }

  @Test(timeout = 20000L)
  public void compares() throws Exception {
    assertEquals(-1, this.compare(1f, 2f));
    assertEquals(0, this.compare(1f, 1f));
    assertEquals(1, this.compare(2f, 1f));
    assertTrue(Float.isNaN(Float.intBitsToFloat(
        this.runBinary(new DADD(), Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY))));
  }

  private int compare(final float left, final float right) throws Exception {
    this.beforeTest();
    this.pushDouble(Float.floatToRawIntBits(left));
    this.pushDouble(Float.floatToRawIntBits(right));
    this.assertLinearExecutionToEnd(this.translate(new DCMPL()));
    final int result = (short) this.pop();
    this.assertStackEmpty();
    return result;
  }

  private void assertFloat(final org.apache.bcel.generic.Instruction instruction, final float left,
                           final float right, final FloatOp operation) throws Exception {
    final int actual = this.runBinary(instruction, left, right);
    final int expected = Float.floatToRawIntBits(operation.apply(left, right));
    assertEquals(instruction.getName() + " " + left + " " + right
            + " expected " + Integer.toHexString(expected) + " actual " + Integer.toHexString(actual),
        expected, actual);
  }

  @FunctionalInterface
  private interface FloatOp {
    float apply(float left, float right);
  }
}
