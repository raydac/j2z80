/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEVIRTUALManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

// class to process INVOKEVIRTUAL with code 182
public class Processor_INVOKEVIRTUAL extends AbstractInvokeProcessor implements NeedsINVOKEVIRTUALManager {

  private final String template;

  public Processor_INVOKEVIRTUAL() {
    super();
    template = loadResourceFileAsString("INVOKEVIRTUAL.a80");
  }

  @Override
  public String getName() {
    return "INVOKEVIRTUAL";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final INVOKEVIRTUAL inv = (INVOKEVIRTUAL) instruction;

    final MethodGen invokedMethod = getInvokedMethod(methodTranslator, inv);

    if (!checkBootstrapCall(methodTranslator, inv, out)) {
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