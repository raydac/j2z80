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

public class MultiArrayRunTest {

  @Test
  public void multidimensionalArraysSupportNestedZeroAndPartialDimensions() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.arrays.MultiArray")
        .file("demo/arrays/MultiArray.java", """
            package demo.arrays;
            
            public class MultiArray {
              public static int fullDimensions;
              public static int fullValues;
              public static int emptyInnerDimensions;
              public static int partialDimensions;
              public static int emptyOuterLength;
            
              public static void mainz() {
                final int[][][] cube = new int[2][2][2];
                cube[0][0][0] = 2;
                cube[1][1][1] = 40;
                fullDimensions = cube.length + cube[0].length + cube[1][1].length;
                fullValues = cube[0][0][0] + cube[1][1][1];
            
                final int[][] emptyInner = new int[3][0];
                emptyInnerDimensions =
                    emptyInner.length + emptyInner[0].length + emptyInner[2].length;
            
                final int[][] partial = new int[2][];
                partial[1] = new int[1];
                partial[1][0] = 7;
                partialDimensions =
                    partial.length + (partial[0] == null ? 10 : 0) + partial[1][0];
            
                final int[][] emptyOuter = new int[0][3];
                emptyOuterLength = emptyOuter.length;
              }
            }
            """)
        .execute();

    assertEquals(6, run.staticInt("demo.arrays.MultiArray", "fullDimensions"));
    assertEquals(42, run.staticInt("demo.arrays.MultiArray", "fullValues"));
    assertEquals(3, run.staticInt("demo.arrays.MultiArray", "emptyInnerDimensions"));
    assertEquals(19, run.staticInt("demo.arrays.MultiArray", "partialDimensions"));
    assertEquals(0, run.staticInt("demo.arrays.MultiArray", "emptyOuterLength"));
  }
}
