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

package com.igormaznitsa.j2z80.translator.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HalfFloatTest {

  @Test
  public void convertsKnownValues() {
    assertEquals(0x0000, HalfFloat.toBits(0f));
    assertEquals(0x8000, HalfFloat.toBits(-0f));
    assertEquals(0x3C00, HalfFloat.toBits(1f));
    assertEquals(0xBC00, HalfFloat.toBits(-1f));
    assertEquals(0x4000, HalfFloat.toBits(2f));
    assertEquals(0x4200, HalfFloat.toBits(3f));
    assertEquals(0x3800, HalfFloat.toBits(0.5f));
    assertEquals(0x7C00, HalfFloat.toBits(Float.POSITIVE_INFINITY));
    assertEquals(0xFC00, HalfFloat.toBits(Float.NEGATIVE_INFINITY));
    assertEquals(0x7BFF, HalfFloat.toBits(65504f));
    assertEquals(0x7C00, HalfFloat.toBits(65536f));
    assertTrue(HalfFloat.isNaN(HalfFloat.toBits(Float.NaN)));
  }

  @Test
  public void roundTripsEveryFinitePattern() {
    for (int bits = 0; bits <= 0xFFFF; bits++) {
      if (HalfFloat.isNaN(bits)) {
        assertTrue(HalfFloat.isNaN(HalfFloat.toBits(HalfFloat.toFloat(bits))));
        continue;
      }
      assertEquals(bits, HalfFloat.toBits(HalfFloat.toFloat(bits)));
    }
  }
}
