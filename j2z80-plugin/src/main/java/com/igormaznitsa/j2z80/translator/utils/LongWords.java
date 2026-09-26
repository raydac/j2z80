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

public final class LongWords {

  private LongWords() {
  }

  public static int requireIntRange(final long value) {
    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Long value does not fit in 32 bits [" + value + ']');
    }
    return (int) value;
  }

  public static String highImmediate(final int value) {
    return Integer.toString((value >>> 16) & 0xFFFF);
  }

  public static String lowImmediate(final int value) {
    return Integer.toString(value & 0xFFFF);
  }
}
