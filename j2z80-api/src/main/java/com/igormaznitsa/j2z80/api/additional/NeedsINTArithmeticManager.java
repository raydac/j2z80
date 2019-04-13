/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
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
