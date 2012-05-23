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
 * The addition shows that an implementing class needs to include the INVOKEINTERFACE
 * manager allows to invoke interface declared methods. 
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("INVOKEINTERFACE_MANAGER.a80")
public interface NeedsINVOKEINTERFACEManager extends J2ZAdditionalBlock {
    /**
     * The label of the subroutine processing invoke interface calls.
     */
    public static final String SUB_INVOKE_INTERFACE = "___INVOKEINTERFACE";
    /**
     * The name of the macros which should be replaced by an the invoke address table label
     */
    public static final String MACROS_INVOKEINTERFACE_TABLE = "%invokeinterfacetable%";
}
