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
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.Writer;

// class to process INVOKESTATIC with code 184
public class Processor_INVOKESTATIC extends AbstractInvokeProcessor implements NeedsMemoryManager {

  private final String template;

  public Processor_INVOKESTATIC() {
    super();
    template = loadResourceFileAsString("INVOKESTATIC.a80");
  }

  @Override
  public String getName() {
    return "INVOKESTATIC";
  }

  public String[] generateCallForStaticInitalizer(final ClassGen classGen) {
    MethodGen initingMethod = null;

    for (final Method method : classGen.getMethods()) {
      if (method.isStatic() && "<clinit>".equals(method.getName()) && method.getArgumentTypes().length == 0 && method.getReturnType().getType() == Type.VOID.getType()) {
        initingMethod = new MethodGen(method, classGen.getClassName(), classGen.getConstantPool());
        break;
      }
    }

    if (initingMethod == null) {
      return new String[0];
    }

    final int argumentMemorySize = calculateArgumentBlockSize(initingMethod);
    final int totalMemorySize = calculateTotalFrameSizeWithoutLocals(initingMethod);
    String prefix = "";
    String postfix = "";


    final String labelForMethod = LabelAndFrameUtils.makeLabelNameForMethod(initingMethod);

    final boolean needsFrame = argumentMemorySize != 0 || totalMemorySize != 0;

    assertLocalVariablesNumber(initingMethod);

    if (needsFrame) {
      prefix = generateFramePrefix(argumentMemorySize, totalMemorySize);
      postfix = generateFramePostfix(argumentMemorySize, totalMemorySize);
    }

    final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix) + '\n';

    return Utils.breakToLines(res);
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final INVOKESTATIC inv = (INVOKESTATIC) instruction;

    final MethodGen invokedMethod = getInvokedMethod(methodTranslator, inv);

    if (!checkBootstrapCall(methodTranslator, inv, out)) {
      assertMethodIsNotNull(invokedMethod, methodTranslator, inv);

      final int argumentMemorySize = calculateArgumentBlockSize(invokedMethod);
      final int totalMemorySize = calculateTotalFrameSizeWithoutLocals(invokedMethod);
      String prefix = "";
      String postfix = "";


      final String labelForMethod = getMethodLabel(methodTranslator, inv);

      final boolean needsFrame = argumentMemorySize != 0 || totalMemorySize != 0;

      assertLocalVariablesNumber(invokedMethod);

      if (needsFrame) {
        prefix = generateFramePrefix(argumentMemorySize, totalMemorySize);
        postfix = generateFramePostfix(argumentMemorySize, totalMemorySize);
      }

      if (invokedMethod.getReturnType().getType() != Type.VOID.getType()) {
        postfix += "PUSH BC\n";
      }

      final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix);

      out.write(res);
      out.write(NEXT_LINE);
    }
  }
}
