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
package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.StringWriter;

public class AbstractInvokeTest extends AbstractJvmCommandProcessorTest implements NeedsMemoryManager, NeedsInstanceofManager {

  public static final Type[] ARGS_NULL = new Type[0];
  public static final String TEST_INVOKED_CLASS = "com.invoked.someclass";
  public static final String TEST_INVOKED_METHOD = "someInvokedMethod";
  public static final int FLAG_ADDRESS = 0x100;
  public static final int FLAG_METHOD_CALLED = 0xFE;
  public static final int INITIAL_IX = 0xCAFE;
  static final Type[] ARGS_FOUR_INT = new Type[] {Type.INT, Type.INT, Type.INT, Type.INT};
  protected static String managerAsmText;

  static {
    try {
      managerAsmText = Utils.readTextResource(AbstractJvmCommandProcessor.class, "MEMORY_MANAGER.a80");
    } catch (IOException ex) {
      throw new Error("Can't read memory manager assembler text");
    }
  }

  public String prepareMemoryManagerText() {
    return managerAsmText.replace(MACRO_INSTANCEOFTABLE, "DEFB 0");
  }

  @Override
  public String getAsmPostfix() {
    return prepareMemoryManagerText();
  }

  public void printStackState(final int depth) {
    int sp = SP;
    for (int i = 0; i < depth; i++) {
      final int value = peekw(sp);
      System.out.println("SP: " + Utils.intToString(sp) + " -> " + Utils.intToString(value));
      sp += 2;
    }
  }

  protected void makePostfixWithBreakPoint(final String breakPoint, final StringWriter out) throws Exception {
    final String processingLabel = LabelAndFrameUtils.makeLabelNameForMethod(mockupOfInvokedMethod.getClassName(), mockupOfInvokedMethod.getName(), mockupOfInvokedMethod.getReturnType(), mockupOfInvokedMethod.getArgumentTypes());

    out.write("JP " + END_LABEL + "\n");
    out.write(processingLabel + ":\n");
    if (breakPoint == null) {
      out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n JP " + END_LABEL + "\n");
    } else {
      out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n" + breakPoint + ": NOP\n RET\n");
    }
  }
}
