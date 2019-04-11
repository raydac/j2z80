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

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.api.additional.NeedsCheckcastManager;
import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;

import java.io.IOException;
import java.io.Writer;

// class to process CHECKCAST with code 192
public class Processor_CHECKCAST extends AbstractJvmCommandProcessor implements NeedsMemoryManager, NeedsCheckcastManager, NeedsInstanceofManager, NeedsATHROWManager {

  private final String template;

  public Processor_CHECKCAST() {
    super();
    template = loadResourceFileAsString("CHECKCAST.a80").replace(MACROS_ADDRESS, SUB_CHECKCAST);
  }

  @Override
  public String getName() {
    return "CHECKCAST";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final CHECKCAST checkcast = (CHECKCAST) instruction;

    final ObjectType type = checkcast.getLoadClassType(methodTranslator.getConstantPool());

    final ClassID castingClassId = new ClassID(type.getClassName());

    methodTranslator.getTranslatorContext().registerClassForCastCheck(castingClassId);

    out.write(template.replace(MACROS_ID, LabelAndFrameUtils.makeLabelForClassID(castingClassId)));
    out.write(NEXT_LINE);
  }
}