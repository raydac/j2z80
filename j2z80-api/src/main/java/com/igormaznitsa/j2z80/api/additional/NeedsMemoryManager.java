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
 * Declares that a translated class depends on the memory-management runtime support shipped in
 * {@code MEMORY_MANAGER.a80}. This block provides the heap allocation, array sizing, stack-frame
 * preparation, and object-release routines used by generated Java code.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("MEMORY_MANAGER.a80")
public interface NeedsMemoryManager extends J2ZAdditionalBlock {
  /**
   * Starts of the heap region managed by the memory manager.
   */
  String MEMORY_HEAP_START_AREA_LABEL = "___MEMORY_HEAP_START_AREA";
  /**
   * Current bump-pointer for the managed heap.
   */
  String VAR_MANAGER_TOP_POINTER = "___MEMORY_MANAGER_TOP_POINTER";
  /**
   * Allocates a two-byte word array in the heap.
   */
  String SUB_ALLOCATE_WORDARRAY = "___MEMORY_ALLOCATE_WORDARRAY";
  /**
   * Allocates a four-byte element array in the heap.
   */
  String SUB_ALLOCATE_DWORDARRAY = "___MEMORY_ALLOCATE_DWORDARRAY";
  /**
   * Allocates a byte array in the heap.
   */
  String SUB_ALLOCATE_BYTEARRAY = "___MEMORY_ALLOCATE_BYTEARRAY";
  /**
   * Returns the length of an array stored in the heap.
   */
  String SUB_GET_ARRAY_LENGTH = "___MEMORY_GET_ARRAY_LENGTH";
  /**
   * Returns the total size of an array payload in bytes.
   */
  String SUB_GET_ARRAY_SIZE = "___MEMORY_GET_ARRAY_SIZE";

  /**
   * Creates a multi-dimensional array from the heap manager runtime.
   */
  String SUB_ALLOCATE_AMULTIARRAY = "___MEMORY_MAKE_WORD_MULTIARRAY";

  /**
   * Allocates a new object instance in the heap.
   */
  String SUB_ALLOCATE_OBJECT = "___MEMORY_ALLOCATE_OBJECT";

  /**
   * Rewinds the heap top back to the header of the supplied instance.
   */
  String SUB_FORGET_OBJECT = "___MEMORY_FORGET_OBJECT";

  /**
   * Returns the class identifier stored for an object.
   */
  String SUB_GET_OBJ_CLASS_ID = "___GET_OBJECT_CLASS_ID";

  /**
   * Returns the in-memory size of an object.
   */
  String SUB_GET_OBJECT_SIZE = "___GET_OBJECT_SIZE";

  /**
   * Finalizes stack state immediately after a method invocation.
   */
  String SUB_AFTER_INVOKE = "___AFTER_INVOKE";

  /**
   * Prepares stack state before a method invocation.
   */
  String SUB_BEFORE_INVOKE = "___BEFORE_INVOKE";

  /**
   * Returns the number of free bytes remaining between the stack pointer and the heap top.
   */
  String SUB_GETFREEMEMORY = "___MEMORY_GET_FREE_MEMORY";
}
