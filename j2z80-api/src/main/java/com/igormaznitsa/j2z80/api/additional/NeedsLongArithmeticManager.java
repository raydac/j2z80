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
 * Declares that a translated class depends on the 32-bit integer arithmetic runtime in
 * {@code LONG_ARITHMETIC_MANAGER.a80}. Java {@code long} values are represented as two 16-bit JVM
 * slots, and this manager provides arithmetic, bitwise, shift, and comparison support.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("LONG_ARITHMETIC_MANAGER.a80")
public interface NeedsLongArithmeticManager extends J2ZAdditionalBlock {
  /**
   * Adds two {@code long} values.
   */
  String SUB_LONG_ADD = "___LONG_ADD";
  /**
   * Subtracts two {@code long} values.
   */
  String SUB_LONG_SUB = "___LONG_SUB";
  /**
   * Multiplies two {@code long} values.
   */
  String SUB_LONG_MUL = "___LONG_MUL";
  /**
   * Divides two {@code long} values.
   */
  String SUB_LONG_DIV = "___LONG_DIV";
  /**
   * Calculates the remainder of two {@code long} values.
   */
  String SUB_LONG_REM = "___LONG_REM";
  /**
   * Negates a {@code long} value.
   */
  String SUB_LONG_NEG = "___LONG_NEG";
  /**
   * Applies bitwise AND to two {@code long} values.
   */
  String SUB_LONG_AND = "___LONG_AND";
  /**
   * Applies bitwise OR to two {@code long} values.
   */
  String SUB_LONG_OR = "___LONG_OR";
  /**
   * Applies bitwise XOR to two {@code long} values.
   */
  String SUB_LONG_XOR = "___LONG_XOR";
  /**
   * Performs a signed left shift on a {@code long} value.
   */
  String SUB_LONG_SHL = "___LONG_SHL";
  /**
   * Performs a signed right shift on a {@code long} value.
   */
  String SUB_LONG_SHR = "___LONG_SHR";
  /**
   * Performs an unsigned right shift on a {@code long} value.
   */
  String SUB_LONG_USHR = "___LONG_USHR";
  /**
   * Compares two {@code long} values.
   */
  String SUB_LONG_CMP = "___LONG_CMP";
}
