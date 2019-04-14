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
