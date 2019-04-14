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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.utils.Utils;

/**
 * The class generates both the prefix and the postfix code for the main method
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MainPrefixPostfixGenerator {
  private final ClassMethodInfo method;
  private final int initStack;
  private final int startAddress;

  /**
   * The Constructor.
   *
   * @param mainMethod    the main method to be used in operations, must not be null
   * @param startAddress  the start address of the main method
   * @param stackInitAddr the value to init stack just after start
   */
  public MainPrefixPostfixGenerator(final ClassMethodInfo mainMethod, final int startAddress, final int stackInitAddr) {
    this.method = mainMethod;
    this.initStack = stackInitAddr;
    this.startAddress = startAddress;
  }

  /**
   * Generate assembler prefix for the main method.
   *
   * @return array of assembler lines to be used as the prefix for the main method
   */
  public String[] generatePrefix() {
    final StringBuilder result = new StringBuilder();

    final int maxLocalsForMainMethod = method.getMethodGen().getMaxLocals();
    final int frameStack = (maxLocalsForMainMethod + 1) << 1;

    result.append("ORG ").append(startAddress).append('\n');
    result.append("DI").append('\n');
    result.append("LD IX,").append(initStack).append('\n');
    result.append("LD SP,").append(initStack - frameStack).append('\n');
    result.append("LD BC,___MAINLOOP___\n").append("PUSH BC\n");

    return Utils.breakToLines(result.toString());
  }

  /**
   * Generate the postfix for the main method.
   *
   * @return array of assembler lines to be used as the postfix for the main method
   */
  public String[] generatePostfix() {
    return Utils.breakToLines("___MAINLOOP___: JP ___MAINLOOP___");
  }

}
