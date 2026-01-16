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

import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEVIRTUALManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

// class to process INVOKEVIRTUAL with code 182
public class Processor_INVOKEVIRTUAL extends AbstractInvokeProcessor implements NeedsINVOKEVIRTUALManager {

  private final String template;

  public Processor_INVOKEVIRTUAL() {
    super();
    this.template = loadResourceFileAsString("INVOKEVIRTUAL.a80");
  }

  @Override
  public String getName() {
    return "INVOKEVIRTUAL";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final INVOKEVIRTUAL inv = (INVOKEVIRTUAL) instruction;

    final MethodGen invokedMethod = this.getInvokedMethod(methodTranslator, inv);

    if (!this.isBootstrapCall(methodTranslator, inv, bootstrapClassLoader, out)) {
      assertMethodIsNotNull(invokedMethod, methodTranslator, inv);
      assertMethodIsNotNull(invokedMethod, methodTranslator, inv);

      final String recordLabel = LabelAndFrameUtils.makeLabelForVirtualMethodRecord(invokedMethod.getClassName(), invokedMethod.getName(), invokedMethod.getReturnType(), invokedMethod.getArgumentTypes());

      final int argumentsBlockSize = calculateArgumentBlockSize(invokedMethod);
      final int offsetOnStackToTheObjectRef = calculateObjectOffsetOnStack(argumentsBlockSize);

      String postfix = "";

      if (invokedMethod.getReturnType().getType() != Type.VOID.getType()) {
        postfix += "PUSH BC\n";
      }

      final String res = template.replace(MACROS_OBJREFOFFSET, "#" + Integer.toHexString(offsetOnStackToTheObjectRef & 0xFFFF).toUpperCase(Locale.ENGLISH))
          .replace(MACROS_RECORDADDR, recordLabel)
          .replace(MACROS_ARGAREALEN, Integer.toString(argumentsBlockSize))
          .replace(MACROS_POSTFIX, postfix);

      out.write(res);
      out.write(NEXT_LINE);
    }
  }
}
