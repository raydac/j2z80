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

import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelNameForField;
import static org.junit.Assert.assertEquals;

import org.apache.bcel.generic.Type;
import org.junit.Test;

public class NumericCornerCaseRunTest {

  @Test
  public void numericBoundaryCasesKeepTheDocumentedRuntimeSemantics() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.numeric.NumericEdges")
        .file("demo/numeric/NumericEdges.java", """
            package demo.numeric;
            
            public class NumericEdges {
              public static int byteHigh;
              public static int byteWrap;
              public static int byteNegativeWrap;
              public static int shortMinimum;
              public static int shortMinusOne;
              public static int charTruncatesToByte;
              public static int charRemainsUnsigned;
              public static int additionWrap;
              public static int multiplicationWrap;
              public static long positiveOverflow;
              public static long negativeOverflow;
              public static long minimumDividedByNegativeOne;
              public static long shiftBy32;
              public static long shiftBy63;
              public static int positiveInfinity;
              public static int negativeInfinity;
              public static int underflowedToZero;
              public static int subnormalRemainsPositive;
              public static int positiveClamp;
              public static int negativeClamp;
              public static int nanConvertsToZero;
              public static int signedZeroEquality;
            
              public static void mainz() {
                narrowingAndWordOverflow();
                longBoundaries();
                halfFloatBoundaries();
              }
            
              private static void narrowingAndWordOverflow() {
                int negativeCharValue = -128;
                int oversizedCharValue = 0x01FF;
                int minimumShortValue = -32768;
                int largestWordValue = 32767;
                int multiplicationLeft = 300;
                int multiplicationRight = 300;
                byteHigh = (byte) 0x80;
                byteWrap = (byte) 0xFF;
                byteNegativeWrap = (byte) -129;
                shortMinimum = (short) minimumShortValue;
                shortMinusOne = (short) -1;
                charTruncatesToByte = (char) oversizedCharValue;
                charRemainsUnsigned = (char) negativeCharValue;
                additionWrap = largestWordValue + 1;
                multiplicationWrap = multiplicationLeft * multiplicationRight;
              }
            
              private static void longBoundaries() {
                long maximum = 2147483647L;
                int shiftCount = 32;
                positiveOverflow = maximum + 1L;
                negativeOverflow = -maximum - 2L;
                minimumDividedByNegativeOne = (-maximum - 1L) / -1L;
                shiftBy32 = 1L << shiftCount;
                shiftCount = 63;
                shiftBy63 = 1L << shiftCount;
              }
            
              private static void halfFloatBoundaries() {
                float maximumFinite = 65504.0f;
                float multiplier = 2.0f;
                float positiveOverflow = maximumFinite * multiplier;
                float negativeOverflow = -maximumFinite * multiplier;
                positiveInfinity = positiveOverflow > 0.0f ? 1 : 0;
                negativeInfinity = negativeOverflow < 0.0f ? 1 : 0;
            
                float underflow = 0.00000001f;
                underflowedToZero = underflow == 0.0f ? 1 : 0;
                float subnormal = 0.00000006f;
                subnormalRemainsPositive = subnormal > 0.0f ? 1 : 0;
            
                float positiveLimit = 32768.0f;
                float negativeLimit = -32768.0f;
                positiveClamp = (int) positiveLimit;
                negativeClamp = (int) negativeLimit;
                float zero = 0.0f;
                float nan = zero / zero;
                nanConvertsToZero = (int) nan;
                signedZeroEquality = -zero == zero ? 1 : 0;
              }
            }
            """)
        .execute();

    assertEquals(-128, run.staticInt("demo.numeric.NumericEdges", "byteHigh"));
    assertEquals(-1, run.staticInt("demo.numeric.NumericEdges", "byteWrap"));
    assertEquals(127, run.staticInt("demo.numeric.NumericEdges", "byteNegativeWrap"));
    assertEquals(-32768, run.staticInt("demo.numeric.NumericEdges", "shortMinimum"));
    assertEquals(-1, run.staticInt("demo.numeric.NumericEdges", "shortMinusOne"));
    assertEquals(255, run.staticInt("demo.numeric.NumericEdges", "charTruncatesToByte"));
    assertEquals(128, run.staticInt("demo.numeric.NumericEdges", "charRemainsUnsigned"));
    assertEquals(-32768, run.staticInt("demo.numeric.NumericEdges", "additionWrap"));
    assertEquals(24464, run.staticInt("demo.numeric.NumericEdges", "multiplicationWrap"));
    assertEquals(Integer.MIN_VALUE, this.staticLong(run, "positiveOverflow"));
    assertEquals(Integer.MAX_VALUE, this.staticLong(run, "negativeOverflow"));
    assertEquals(Integer.MIN_VALUE, this.staticLong(run, "minimumDividedByNegativeOne"));
    assertEquals(0, this.staticLong(run, "shiftBy32"));
    assertEquals(0, this.staticLong(run, "shiftBy63"));
    assertEquals(1, run.staticInt("demo.numeric.NumericEdges", "positiveInfinity"));
    assertEquals(1, run.staticInt("demo.numeric.NumericEdges", "negativeInfinity"));
    assertEquals(1, run.staticInt("demo.numeric.NumericEdges", "underflowedToZero"));
    assertEquals(1, run.staticInt("demo.numeric.NumericEdges", "subnormalRemainsPositive"));
    assertEquals(32767, run.staticInt("demo.numeric.NumericEdges", "positiveClamp"));
    assertEquals(-32768, run.staticInt("demo.numeric.NumericEdges", "negativeClamp"));
    assertEquals(0, run.staticInt("demo.numeric.NumericEdges", "nanConvertsToZero"));
    assertEquals(1, run.staticInt("demo.numeric.NumericEdges", "signedZeroEquality"));
  }

  private int staticLong(final JavaZ80Run run, final String fieldName) {
    final int fieldAddress = run.addressOf(
        makeLabelNameForField("demo.numeric.NumericEdges", fieldName, Type.LONG));
    final int lowWord = run.wordAtAddress(fieldAddress);
    final int highWord = run.wordAtAddress(fieldAddress + 2);
    return (highWord << 16) | lowWord;
  }
}
