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
 * The interface describes an assembler translator.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface AsmTranslator {

  /**
   * Find a label address for its name
   *
   * @param labelName the label name, must not be null
   * @return the found address or null if the label is not defined
   */
  Integer findLabelAddress(String labelName);

  /**
   * Register a label address for a name in the global label table
   *
   * @param labelName the label name, must not be null
   * @param address   the label address
   */
  void registerGlobalLabelAddress(String labelName, int address);

  /**
   * Register a label address for a name in the local label table
   *
   * @param labelName the label name, must not be null
   * @param address   the label address
   */
  void registerLocalLabelAddress(String labelName, int address);

  /**
   * Register an object who needs to be informed only time when some label is registered
   *
   * @param label    the label name, must not be null
   * @param listener the listener to be informed about the label registration
   */
  void registerLocalLabelExpectant(String label, LocalLabelExpectant listener);

  /**
   * Get the current state of the program counter (PC)
   *
   * @return the current state of the program counter
   */
  int getPC();

  /**
   * Set the state for the program counter (PC)
   *
   * @param newPCValue the new value for the PC
   */
  void setPC(int newPCValue);

  /**
   * Write byte array into result data
   *
   * @param code the array to be written, must not be null
   */
  void writeCode(byte[] code);

  /**
   * Get the current written code block size in bytes
   *
   * @return the current code block size in bytes
   */
  int getCodeSize();

  /**
   * Get the offset in bytes to the first translated byte
   *
   * @return the offset to the translated code block start
   */
  int getDataOffset();

  /**
   * Get the start address of the translated code
   *
   * @return the start address
   */
  int getEntryPoint();

  /**
   * Set the address to execute translated code
   *
   * @param address the start address for the translated code
   */
  void setEntryPoint(int address);

  /**
   * Print some text to an output device
   *
   * @param text the text to be printed, must not be null
   */
  void printText(String text);

  /**
   * Remove all registered local variables from the inside table
   */
  void clearLocalLabels();
}
