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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.Writer;

// class to process INVOKESPECIAL with code 183
public class Processor_INVOKESPECIAL extends AbstractInvokeProcessor implements NeedsMemoryManager {

  private final String template;

  public Processor_INVOKESPECIAL() {
    super();
    template = loadResourceFileAsString("INVOKESPECIAL.a80");
  }

  @Override
  public String getName() {
    return "INVOKESPECIAL";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final INVOKESPECIAL inv = (INVOKESPECIAL) instruction;
    final MethodGen invokingMethod = getInvokedMethod(methodTranslator, inv);

    if (!checkBootstrapCall(methodTranslator, inv, out)) {
      assertMethodIsNotNull(invokingMethod, methodTranslator, inv);

      final String labelForMethod = getMethodLabel(methodTranslator, inv);

      final int argBlockSize = calculateArgumentBlockSize(invokingMethod);
      final int frameSize = calculateTotalFrameSizeWithoutLocals(invokingMethod);

      assertLocalVariablesNumber(invokingMethod);

      final String prefix = generateFramePrefix(argBlockSize, frameSize);
      String postfix = generateFramePostfix(argBlockSize, frameSize);

      if (invokingMethod.getReturnType().getType() != Type.VOID.getType()) {
        postfix += "PUSH BC\n";
      }

      final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix);

      out.write(res);
      out.write(NEXT_LINE);
    }
  }
}
