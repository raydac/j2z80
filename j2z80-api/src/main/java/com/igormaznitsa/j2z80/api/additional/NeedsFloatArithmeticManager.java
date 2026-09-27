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
 * Shows that an implementing class needs the binary16 float arithmetic manager.
 * Each float occupies one 16-bit JVM slot.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("FLOAT_ARITHMETIC_MANAGER.a80")
public interface NeedsFloatArithmeticManager extends J2ZAdditionalBlock {
  String SUB_FLOAT_ADD = "___FLOAT_ADD";
  String SUB_FLOAT_SUB = "___FLOAT_SUB";
  String SUB_FLOAT_MUL = "___FLOAT_MUL";
  String SUB_FLOAT_DIV = "___FLOAT_DIV";
  String SUB_FLOAT_REM = "___FLOAT_REM";
  String SUB_FLOAT_I2F = "___FLOAT_I2F";
  String SUB_FLOAT_F2I = "___FLOAT_F2I";
  String SUB_FLOAT_CMP = "___FLOAT_CMP";
}
