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
 * Declares that a translated class depends on the binary32 double-precision arithmetic runtime in
 * {@code DOUBLE_ARITHMETIC_MANAGER.a80}. Java {@code double} values are represented across two
 * 16-bit JVM slots, and this manager supplies the arithmetic, comparison, and conversion helpers
 * the generated code requires.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("DOUBLE_ARITHMETIC_MANAGER.a80")
public interface NeedsDoubleArithmeticManager extends J2ZAdditionalBlock {
  /**
   * Adds two {@code double} values.
   */
  String SUB_DOUBLE_ADD = "___DBL_ADD";
  /**
   * Subtracts two {@code double} values.
   */
  String SUB_DOUBLE_SUB = "___DBL_SUB";
  /**
   * Multiplies two {@code double} values.
   */
  String SUB_DOUBLE_MUL = "___DBL_MUL";
  /**
   * Divides two {@code double} values.
   */
  String SUB_DOUBLE_DIV = "___DBL_DIV";
  /**
   * Calculates the remainder of two {@code double} values.
   */
  String SUB_DOUBLE_REM = "___DBL_REM";
  /**
   * Compares two {@code double} values.
   */
  String SUB_DOUBLE_CMP = "___DBL_CMP";
  /**
   * Converts an {@code int} to {@code double}.
   */
  String SUB_DOUBLE_I2D = "___DBL_I2D";
  /**
   * Converts a {@code long} to {@code double}.
   */
  String SUB_DOUBLE_L2D = "___DBL_L2D";
  /**
   * Converts a {@code float} to {@code double}.
   */
  String SUB_DOUBLE_F2D = "___DBL_F2D";
  /**
   * Converts a {@code double} to {@code int}.
   */
  String SUB_DOUBLE_D2I = "___DBL_D2I";
  /**
   * Converts a {@code double} to {@code long}.
   */
  String SUB_DOUBLE_D2L = "___DBL_D2L";
  /**
   * Converts a {@code double} to {@code float}.
   */
  String SUB_DOUBLE_D2F = "___DBL_D2F";
}
