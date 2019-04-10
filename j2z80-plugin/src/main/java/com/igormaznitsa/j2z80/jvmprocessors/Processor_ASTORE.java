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
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import java.io.IOException;
import java.io.Writer;

// class to process ASTORE with code 75,76,77,78
public class Processor_ASTORE extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_ASTORE() {
    super();
    template = loadResourceFileAsString("ASTORE.a80");
  }

  @Override
  public String getName() {
    return "ASTORE";
  }

  @Override
  public void process(final MethodTranslator classProcessor, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final ASTORE astore = (ASTORE) instruction;
    out.write(template.replace(MACROS_INDEX, Integer.toString(prepareLocalVariableIndex(astore.getIndex()))));
    out.write(NEXT_LINE);
  }
}
