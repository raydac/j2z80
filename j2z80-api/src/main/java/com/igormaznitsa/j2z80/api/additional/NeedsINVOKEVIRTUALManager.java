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
 * Declares that a translated class depends on the runtime support used for Java virtual dispatch.
 * The manager resolves the correct method implementation based on the receiver's class and the
 * selected method signature.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("INVOKEVIRTUAL_MANAGER.a80")
public interface NeedsINVOKEVIRTUALManager extends J2ZAdditionalBlock {
  /**
   * Macro placeholder replaced with the generated virtual dispatch table label.
   */
  String MACROS_INVOKEVIRTUAL_TABLE = "%invokevirtualtable%";
}
