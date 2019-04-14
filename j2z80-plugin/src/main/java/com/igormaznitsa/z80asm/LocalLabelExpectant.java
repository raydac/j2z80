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
package com.igormaznitsa.z80asm;

/**
 * The interface describes a local label expectant, it works like an observer but only one time.
 *
 * @author Imgor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface LocalLabelExpectant {
  /**
   * This method will be called when the needed label is registered.
   *
   * @param asmTranslator the assembler translator, must not be null
   * @param labelName     the label name, must not be null
   * @param labelAddress  the label address
   */
  void onLabelIsAccessible(AsmTranslator asmTranslator, String labelName, long labelAddress);
}
