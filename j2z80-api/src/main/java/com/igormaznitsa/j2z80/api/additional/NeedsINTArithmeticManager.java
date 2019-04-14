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
package com.igormaznitsa.j2z80.api.additional;

/**
 * The addition shows that an implementing class needs to include the integer arithmetic manager.
 * The manager allows to emulate MUL, DIV and REM signed integer operations on Z80.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("INT_ARITHMETIC_MANAGER.a80")
public interface NeedsINTArithmeticManager extends J2ZAdditionalBlock {
  /**
   * The label of the MUL emulating subroutine
   */
  public static final String SUB_INT_MUL = "___INT_MATH_MUL";
  /**
   * The label of the DIV emulating subroutine
   */
  public static final String SUB_INT_DIV = "___INT_MATH_DIV";
  /**
   * The label of the REM emulating subroutine
   */
  public static final String SUB_INT_REM = "___INT_MATH_REM";
}
