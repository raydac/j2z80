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

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEINTERFACEManager;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

// class to process INVOKEINTERFACE with code 185
public class Processor_INVOKEINTERFACE extends AbstractInvokeProcessor implements NeedsATHROWManager, NeedsINVOKEINTERFACEManager {

  private final String template;

  public Processor_INVOKEINTERFACE() {
    super();
    template = loadResourceFileAsString("INVOKEINTERFACE.a80");
  }

  @Override
  public String getName() {
    return "INVOKEINTERFACE";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final INVOKEINTERFACE inv = (INVOKEINTERFACE) instruction;

    final MethodGen invokedMethod = getInvokedMethod(methodTranslator, inv);

    if (!checkBootstrapCall(methodTranslator, inv, out)) {
      assertMethodIsNotNull(invokedMethod, methodTranslator, inv);
      final MethodID interfaceMethodId = new MethodID(invokedMethod);
      methodTranslator.getTranslatorContext().registerInterfaceMethodForINVOKEINTERFACE(interfaceMethodId);

      final String interfaceMethdodId = LabelAndFrameUtils.makeLabelForMethodID(interfaceMethodId);

      final int argumentsBlockSize = calculateArgumentBlockSize(invokedMethod);

      assertLocalVariablesNumber(invokedMethod);

      final int offsetOnStackToTheObjectRef = calculateObjectOffsetOnStack(argumentsBlockSize);

      String postfix = "";

      if (invokedMethod.getReturnType().getType() != Type.VOID.getType()) {
        postfix += "PUSH BC\n";
      }

      final String res = template.replace(MACROS_OBJREFOFFSET, "#" + Integer.toHexString(offsetOnStackToTheObjectRef & 0xFFFF).toUpperCase(Locale.ENGLISH)).replace(MACROS_VALUE, Integer.toString(argumentsBlockSize)).replace(MACROS_ID, interfaceMethdodId).replace(MACROS_POSTFIX, postfix);

      out.write(res);
      out.write(NEXT_LINE);
    }
  }
}
