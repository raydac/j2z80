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
 * The addition shows that an implementing class needs to include the INVOKEVIRTUAL manager.
 * The manager allows to invoke virtual methods of classes.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@AdditionPath("INVOKEVIRTUAL_MANAGER.a80")
public interface NeedsINVOKEVIRTUALManager extends J2ZAdditionalBlock {
  /**
   * The macros name which should be replaced by either label or address of an invoke virtual table
   */
  public static final String MACROS_INVOKEVIRTUAL_TABLE = "%invokevirtualtable%";
}
