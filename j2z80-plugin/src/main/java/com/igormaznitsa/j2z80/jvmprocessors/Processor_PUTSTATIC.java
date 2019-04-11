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

import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTSTATIC;

import java.io.IOException;
import java.io.Writer;

// class to process PUTSTATIC with code 179
public class Processor_PUTSTATIC extends AbstractFieldProcessor {

  private final String template;

  public Processor_PUTSTATIC() {
    super();
    template = loadResourceFileAsString("PUTSTATIC.a80");
  }

  @Override
  public String getName() {
    return "PUTSTATIC";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final PUTSTATIC putstatic = (PUTSTATIC) instruction;
    if (!processBootClassCall(methodTranslator, putstatic, out)) {
      final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
      final ObjectType objType = (ObjectType) putstatic.getReferenceType(constantPool);

      final String label = LabelAndFrameUtils.makeLabelNameForField(objType.getClassName(), putstatic.getFieldName(constantPool), putstatic.getFieldType(constantPool));

      out.write(template.replace(MACROS_ADDRESS, label));
      out.write(NEXT_LINE);
    }
  }
}