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
 * The addition shows that an implementing class needs to include the BREAKPOINT command
 * manager.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@J2Z80AdditionPath("BREAKPOINT_MANAGER.a80")
public interface NeedsBREAKPOINTManager extends J2ZAdditionalBlock {
  /**
   * The label of the memory cells containing the current breakpoint processing subroutine address
   */
  String BREAKPOINT_PROCESSING_SUB_ADDRESS = "___BREAKPOINT_PROCESSING_CODE_ADDRESS";

  /**
   * The label of the breakpoint processing subroutine stub (it does nothing)
   */
  String BREAKPOINT_PROCESSING_STUB = "___BREAKPOINT_PROCESSING_STUB";

  /**
   * The label of the breakpoint processing manager subroutine
   */
  String BREAKPOINT_PROCESSING_MANAGER = "___BREAKPOINT_PROCESSING_MANAGER";
}
