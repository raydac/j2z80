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

package com.igormaznitsa.j2z80.translator.utils;

/**
 * IEEE 754 binary16 bit patterns. The translator stores each Java {@code float} in one 16-bit slot.
 */
public final class HalfFloat {

  public static final int POSITIVE_INFINITY = 0x7C00;
  public static final int NEGATIVE_INFINITY = 0xFC00;
  public static final int NAN = 0x7E00;

  private HalfFloat() {
  }

  public static boolean isNaN(final int halfBits) {
    return ((halfBits & 0x7C00) == 0x7C00) && ((halfBits & 0x03FF) != 0);
  }

  public static int toBits(final float value) {
    final int bits = Float.floatToRawIntBits(value);
    final int sign = (bits >>> 16) & 0x8000;
    final int exponent = (bits >>> 23) & 0xFF;
    final int fraction = bits & 0x007FFFFF;

    if (exponent == 0xFF) {
      if (fraction == 0) {
        return sign | POSITIVE_INFINITY;
      }
      final int payload = fraction >>> 13;
      return sign | 0x7C00 | (payload == 0 ? 0x200 : payload);
    }

    if (exponent == 0 && fraction == 0) {
      return sign;
    }

    final int halfExponent = exponent - 127 + 15;
    if (halfExponent >= 0x1F) {
      return sign | POSITIVE_INFINITY;
    }
    if (halfExponent <= 0) {
      return sign | encodeSubnormal(exponent == 0 ? fraction : (fraction | 0x800000), halfExponent);
    }

    int halfFraction = fraction >>> 13;
    if (roundsUp(fraction & 0x1FFF, halfFraction)) {
      halfFraction++;
      if (halfFraction == 0x400) {
        if (halfExponent + 1 >= 0x1F) {
          return sign | POSITIVE_INFINITY;
        }
        return sign | ((halfExponent + 1) << 10);
      }
    }
    return sign | (halfExponent << 10) | halfFraction;
  }

  public static float toFloat(final int halfBits) {
    final int bits = halfBits & 0xFFFF;
    final int sign = (bits & 0x8000) << 16;
    final int exponent = (bits >>> 10) & 0x1F;
    int fraction = bits & 0x03FF;

    if (exponent == 0x1F) {
      return Float.intBitsToFloat(sign | 0x7F800000 | (fraction << 13));
    }
    if (exponent == 0) {
      if (fraction == 0) {
        return Float.intBitsToFloat(sign);
      }
      int subnormalExponent = -14;
      while ((fraction & 0x400) == 0) {
        fraction <<= 1;
        subnormalExponent--;
      }
      fraction &= 0x3FF;
      return Float.intBitsToFloat(sign | ((subnormalExponent + 127) << 23) | (fraction << 13));
    }
    return Float.intBitsToFloat(sign | ((exponent - 15 + 127) << 23) | (fraction << 13));
  }

  public static String toAsmImmediate(final float value) {
    return Integer.toString(toBits(value));
  }

  private static int encodeSubnormal(final int significand, final int halfExponent) {
    final int shift = 14 - halfExponent;
    if (shift >= 25) {
      return 0;
    }
    int halfFraction = significand >>> shift;
    final int guard = (significand >>> (shift - 1)) & 1;
    final int stickyMask = (1 << (shift - 1)) - 1;
    final int sticky = (significand & stickyMask) != 0 ? 1 : 0;
    if (guard == 1 && (sticky == 1 || (halfFraction & 1) == 1)) {
      halfFraction++;
    }
    return halfFraction;
  }

  private static boolean roundsUp(final int discarded, final int halfFraction) {
    final int guard = discarded >>> 12;
    final int sticky = (discarded & 0x0FFF) != 0 ? 1 : 0;
    return guard == 1 && (sticky == 1 || (halfFraction & 1) == 1);
  }
}
