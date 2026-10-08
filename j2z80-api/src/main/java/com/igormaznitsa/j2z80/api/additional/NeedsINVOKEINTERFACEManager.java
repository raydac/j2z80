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
 * Declares that a translated class depends on the runtime support used for Java
 * {@code invokeinterface} dispatch. The manager resolves interface method tables and executes the
 * selected dispatch target for the current object instance.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("INVOKEINTERFACE_MANAGER.a80")
public interface NeedsINVOKEINTERFACEManager extends J2ZAdditionalBlock {
  /**
   * Dispatch routine for a dynamic interface call.
   */
  String SUB_INVOKE_INTERFACE = "___INVOKEINTERFACE";
  /**
   * Macro placeholder that is replaced with the generated interface dispatch table label.
   */
  String MACROS_INVOKEINTERFACE_TABLE = "%invokeinterfacetable%";
}
