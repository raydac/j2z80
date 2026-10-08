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
 * Declares that a translated class depends on the signed integer arithmetic runtime in
 * {@code INT_ARITHMETIC_MANAGER.a80}. The manager emulates multiply, divide, and remainder
 * operations that are not directly expressible with the Z80 instruction set.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("INT_ARITHMETIC_MANAGER.a80")
public interface NeedsINTArithmeticManager extends J2ZAdditionalBlock {
  /**
   * Runtime label implementing integer multiplication.
   */
  String SUB_INT_MUL = "___INT_MATH_MUL";
  /**
   * Runtime label implementing integer division.
   */
  String SUB_INT_DIV = "___INT_MATH_DIV";
  /**
   * Runtime label implementing integer remainder.
   */
  String SUB_INT_REM = "___INT_MATH_REM";
}
