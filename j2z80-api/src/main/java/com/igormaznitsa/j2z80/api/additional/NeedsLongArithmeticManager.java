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
 * Shows that an implementing class needs the 32-bit long arithmetic manager.
 * Each Java long occupies two 16-bit JVM slots.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("LONG_ARITHMETIC_MANAGER.a80")
public interface NeedsLongArithmeticManager extends J2ZAdditionalBlock {
  String SUB_LONG_ADD = "___LONG_ADD";
  String SUB_LONG_SUB = "___LONG_SUB";
  String SUB_LONG_MUL = "___LONG_MUL";
  String SUB_LONG_DIV = "___LONG_DIV";
  String SUB_LONG_REM = "___LONG_REM";
  String SUB_LONG_NEG = "___LONG_NEG";
  String SUB_LONG_AND = "___LONG_AND";
  String SUB_LONG_OR = "___LONG_OR";
  String SUB_LONG_XOR = "___LONG_XOR";
  String SUB_LONG_SHL = "___LONG_SHL";
  String SUB_LONG_SHR = "___LONG_SHR";
  String SUB_LONG_USHR = "___LONG_USHR";
  String SUB_LONG_CMP = "___LONG_CMP";
}
