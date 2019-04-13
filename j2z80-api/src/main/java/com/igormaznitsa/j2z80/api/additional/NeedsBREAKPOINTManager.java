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
 * The addition shows that an implementing class needs to include the BREAKPOINT command
 * manager.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("BREAKPOINT_MANAGER.a80")
public interface NeedsBREAKPOINTManager extends J2ZAdditionalBlock {
  /**
   * The label of the memory cells containing the current breakpoint processing subroutine address
   */
  public static final String BREAKPOINT_PROCESSING_SUB_ADDRESS = "___BREAKPOINT_PROCESSING_CODE_ADDRESS";

  /**
   * The label of the breakpoint processing subroutine stub (it does nothing)
   */
  public static final String BREAKPOINT_PROCESSING_STUB = "___BREAKPOINT_PROCESSING_STUB";

  /**
   * The label of the breakpoint processing manager subroutine
   */
  public static final String BREAKPOINT_PROCESSING_MANAGER = "___BREAKPOINT_PROCESSING_MANAGER";
}
