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
 * The addition shows that an implementing class needs to include the memory
 * manager. The manager implements all memory operations.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("MEMORY_MANAGER.a80")
public interface NeedsMemoryManager extends J2ZAdditionalBlock {
  /**
   * The label of the memory cells which is the start point for the memory heap
   */
  public static final String MEMORY_HEAP_START_AREA_LABEL = "___MEMORY_HEAP_START_AREA";
  /**
   * The label of the memory cell contains the first memory heap free address
   */
  public static final String VAR_MANAGER_TOP_POINTER = "___MEMORY_MANAGER_TOP_POINTER";
  /**
   * The label of the subroutine to allocate a word array (a word is two bytes) in the heap
   */
  public static final String SUB_ALLOCATE_WORDARRAY = "___MEMORY_ALLOCATE_WORDARRAY";
  /**
   * The label of the subroutine to allocate a byte array in the heap
   */
  public static final String SUB_ALLOCATE_BYTEARRAY = "___MEMORY_ALLOCATE_BYTEARRAY";
  /**
   * The label of the subroutine to get number of elements in an array
   */
  public static final String SUB_GET_ARRAY_LENGTH = "___MEMORY_GET_ARRAY_LENGTH";
  /**
   * The label of the subroutine to get the size in bytes of an array
   */
  public static final String SUB_GET_ARRAY_SIZE = "___MEMORY_GET_ARRAY_SIZE";

  /**
   * The label of the subroutine to make a multi dimension array in the heap
   */
  public static final String SUB_ALLOCATE_AMULTIARRAY = "___MEMORY_MAKE_WORD_MULTIARRAY";

  /**
   * The label of the subroutine to make a new class instance in the heap
   */
  public static final String SUB_ALLOCATE_OBJECT = "___MEMORY_ALLOCATE_OBJECT";

  /**
   * The label of the subroutine to get the class UID of an object in the heap
   */
  public static final String SUB_GET_OBJ_CLASS_ID = "___GET_OBJECT_CLASS_ID";

  /**
   * The label of the subroutine to get the size of an object in bytes
   */
  public static final String SUB_GET_OBJECT_SIZE = "___GET_OBJECT_SIZE";

  /**
   * The label of the subroutine to process stack state after an invoke operation
   */
  public static final String SUB_AFTER_INVOKE = "___AFTER_INVOKE";

  /**
   * The label of the subroutine to process stack and prepare it for an invoke operation
   */
  public static final String SUB_BEFORE_INVOKE = "___BEFORE_INVOKE";

  /**
   * The label of the subroutine to get the free memory size in bytes (SP  - the current heap top address)
   */
  public static final String SUB_GETFREEMEMORY = "___MEMORY_GET_FREE_MEMORY";
}
