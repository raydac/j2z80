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
 * Declares that a translated class depends on the debug breakpoint support in
 * {@code BREAKPOINT_MANAGER.a80}. This runtime block allows the generated code to hook debug
 * breakpoints without changing the normal execution path.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("BREAKPOINT_MANAGER.a80")
public interface NeedsBREAKPOINTManager extends J2ZAdditionalBlock {
  /**
   * Address slot that stores the current breakpoint callback routine.
   */
  String BREAKPOINT_PROCESSING_SUB_ADDRESS = "___BREAKPOINT_PROCESSING_CODE_ADDRESS";

  /**
   * No-op stub used when no breakpoint handler is active.
   */
  String BREAKPOINT_PROCESSING_STUB = "___BREAKPOINT_PROCESSING_STUB";

  /**
   * Runtime entry point for the breakpoint manager.
   */
  String BREAKPOINT_PROCESSING_MANAGER = "___BREAKPOINT_PROCESSING_MANAGER";
}
