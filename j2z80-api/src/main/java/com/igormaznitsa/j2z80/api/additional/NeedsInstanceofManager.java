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
 * The addition shows that an implementing class needs to include the INSTANCEOF manager. 
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("INSTANCEOF_MANAGER.a80")
public interface NeedsInstanceofManager extends J2ZAdditionalBlock {
    /**
     * The macros to be replaced by an instanceof table represented as String.
     */ 
    public static final String MACRO_INSTANCEOFTABLE = "%instanceoftable%";
    /**
     * The label of the memory address where the instanceof table will be placed
     */
    public static final String INSTANCEOF_TABLE_POINTER = "___INSTANCEOF_TABLE";
    /**
     * The label of the subroutine processing the instanceof logic
     */
    public static final String SUB_INSTANCEOF = "___INSTANCE_OF";
}
