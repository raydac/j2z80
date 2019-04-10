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
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import java.io.IOException;
import java.io.Writer;

// class to process ATHROW with code 191
public class Processor_ATHROW extends AbstractJvmCommandProcessor implements NeedsATHROWManager {
  private final String template;

  public Processor_ATHROW() {
    super();
    template = loadResourceFileAsString("ATHROW.a80").replace(MACROS_ADDRESS, ATHROW_PROCESSING_ADDRESS);
  }

  @Override
  public String getName() {
    return "ATHROW";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    methodTranslator.getTranslatorContext().getLogger().logWarning("ATHROW in usage, don't forget define its processing");
    final ATHROW athrow = (ATHROW) instruction;
    out.write(template);
    out.write(NEXT_LINE);
  }
}
