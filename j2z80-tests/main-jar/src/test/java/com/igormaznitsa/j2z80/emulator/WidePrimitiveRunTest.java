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

package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WidePrimitiveRunTest {

  @Test
  public void widePrimitivesSurviveMixedWidthCallsFieldsAndArrays() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.numeric.NumericFlow")
        .file("demo/numeric/NumericBox.java", """
            package demo.numeric;
            
            public class NumericBox {
              private long base;
              private double scale;
              private float bias;
            
              public NumericBox(final long base, final double scale, final float bias) {
                this.base = base;
                this.scale = scale;
                this.bias = bias;
              }
            
              public long adjust(final long delta, final int factor) {
                final long staged = this.base + delta;
                final long scaled = staged * factor;
                return scaled - this.base;
              }
            
              public double blend(final double delta, final long divisor) {
                final double staged = this.scale + delta;
                return staged / divisor;
              }
            
              public float shift(final float delta, final int repeat) {
                final float staged = this.bias + delta;
                return staged * repeat;
              }
            }
            """)
        .file("demo/numeric/NumericFlow.java", """
            package demo.numeric;
            
            public class NumericFlow {
              public static int longResult;
              public static int doubleResult;
              public static int doubleRemainder;
              public static int floatResult;
              public static int floatNanComparison;
            
              public static void mainz() {
                final NumericBox box = new NumericBox(10L, 3.0, 1.5f);
            
                final long[] longValues = new long[2];
                longValues[0] = box.adjust(5L, 2);
                longValues[1] = box.adjust(7L, 2);
                longResult = (int) combine(longValues[0], longValues[1]);
            
                final double[] doubleValues = new double[2];
                doubleValues[0] = box.blend(1.0, 2L);
                doubleValues[1] = box.blend(doubleValues[0], 2L);
                doubleResult = doubleValues[1] == 2.5 ? 1 : 0;
                doubleRemainder = 5.5 % 2.0 == 1.5 ? 1 : 0;
            
                final float[] floatValues = new float[2];
                floatValues[0] = box.shift(0.5f, 3);
                floatValues[1] = box.shift(floatValues[0], 1);
                floatResult = (int) (floatValues[1] * 10.0f);
            
                final float nan = 0.0f / 0.0f;
                floatNanComparison = nan != nan ? 1 : 0;
              }
            
              private static long combine(final long first, final long second) {
                final long product = first * second;
                final long remainder = product % 7L;
                return remainder + (first << 2) - product;
              }
            }
            """)
        .execute();

    assertEquals(-396, run.staticInt("demo.numeric.NumericFlow", "longResult"));
    assertEquals(1, run.staticInt("demo.numeric.NumericFlow", "doubleResult"));
    assertEquals(1, run.staticInt("demo.numeric.NumericFlow", "doubleRemainder"));
    assertEquals(75, run.staticInt("demo.numeric.NumericFlow", "floatResult"));
    assertEquals(1, run.staticInt("demo.numeric.NumericFlow", "floatNanComparison"));
  }
}
