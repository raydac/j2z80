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

package com.igormaznitsa.j2z80.api.additional;

/**
 * Declares that a translated class depends on the binary16 float arithmetic runtime in
 * {@code FLOAT_ARITHMETIC_MANAGER.a80}. Java {@code float} values are stored in a single 16-bit
 * JVM slot and use this manager for arithmetic, comparison, and conversion helpers.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("FLOAT_ARITHMETIC_MANAGER.a80")
public interface NeedsFloatArithmeticManager extends J2ZAdditionalBlock {
  /**
   * Adds two {@code float} values.
   */
  String SUB_FLOAT_ADD = "___FLOAT_ADD";
  /**
   * Subtracts two {@code float} values.
   */
  String SUB_FLOAT_SUB = "___FLOAT_SUB";
  /**
   * Multiplies two {@code float} values.
   */
  String SUB_FLOAT_MUL = "___FLOAT_MUL";
  /**
   * Divides two {@code float} values.
   */
  String SUB_FLOAT_DIV = "___FLOAT_DIV";
  /**
   * Calculates the remainder of two {@code float} values.
   */
  String SUB_FLOAT_REM = "___FLOAT_REM";
  /**
   * Converts an {@code int} to {@code float}.
   */
  String SUB_FLOAT_I2F = "___FLOAT_I2F";
  /**
   * Converts a {@code float} to {@code int}.
   */
  String SUB_FLOAT_F2I = "___FLOAT_F2I";
  /**
   * Compares two {@code float} values.
   */
  String SUB_FLOAT_CMP = "___FLOAT_CMP";
}
