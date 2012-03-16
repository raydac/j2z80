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

@AdditionPath("MEMORY_MANAGER.a80")
public interface NeedsMemoryManager extends J2ZAdditionalBlock {
    public static final String MEMORY_HEAP_START_AREA_LABEL = "___MEMORY_HEAP_START_AREA";
    public static final String VAR_MANAGER_TOP_POINTER = "___MEMORY_MANAGER_TOP_POINTER";
    
    public static final String SUB_ALLOCATE_WORDARRAY = "___MEMORY_ALLOCATE_WORDARRAY";
    public static final String SUB_ALLOCATE_BYTEARRAY = "___MEMORY_ALLOCATE_BYTEARRAY";
    public static final String SUB_GET_ARRAY_LENGTH = "___MEMORY_GET_ARRAY_LENGTH";
    public static final String SUB_GET_ARRAY_SIZE = "___MEMORY_GET_ARRAY_SIZE";
    public static final String SUB_ALLOCATE_AMULTIARRAY = "___MEMORY_MAKE_WORD_MULTIARRAY";
    
    public static final String SUB_ALLOCATE_OBJECT = "___MEMORY_ALLOCATE_OBJECT";
    public static final String SUB_GET_OBJ_CLASS_ID = "___GET_OBJECT_CLASS_ID";
    public static final String SUB_GET_OBJECT_SIZE = "___GET_OBJECT_SIZE";
    
    public static final String SUB_AFTER_INVOKE = "___AFTER_INVOKE";
    public static final String SUB_BEFORE_INVOKE = "___BEFORE_INVOKE";
    
    public static final String SUB_GETFREEMEMORY = "___MEMORY_GET_FREE_MEMORY";
}
