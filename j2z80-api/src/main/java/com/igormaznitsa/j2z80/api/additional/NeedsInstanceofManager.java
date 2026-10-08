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
 * Declares that a translated class depends on the runtime support used to implement Java
 * {@code instanceof} checks. The manager emits and resolves the class-identity tables used by
 * generated cast and type-check instructions.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("INSTANCEOF_MANAGER.a80")
public interface NeedsInstanceofManager extends J2ZAdditionalBlock {
  /**
   * Marker used by the generator to substitute the generated {@code instanceof} table.
   */
  String MACRO_INSTANCEOFTABLE = "%instanceoftable%";
  /**
   * Memory label where the generated {@code instanceof} table is written.
   */
  String INSTANCEOF_TABLE_POINTER = "___INSTANCEOF_TABLE";
  /**
   * Dispatch label for the runtime {@code instanceof} routine.
   */
  String SUB_INSTANCEOF = "___INSTANCE_OF";
}
