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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.NEWARRAY;

import java.io.IOException;
import java.io.Writer;

// class to process NEWARRAY with code 188
public class Processor_NEWARRAY extends AbstractJvmCommandProcessor implements NeedsMemoryManager {
  private final String template;

  public Processor_NEWARRAY() {
    super();
    template = loadResourceFileAsString("NEWARRAY.a80");
  }

  @Override
  public String getName() {
    return "NEWARRAY";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final NEWARRAY newarray = (NEWARRAY) instruction;

    final int arrayType = newarray.getTypecode();

    String sub;

    switch (arrayType) {
      case 4: // boolean
      case 5: // char
      case 8: // byte
      {
        sub = SUB_ALLOCATE_BYTEARRAY;
      }
      break;
      case 9: // short
      case 10: // int
      {
        sub = SUB_ALLOCATE_WORDARRAY;
      }
      break;
      default: {
        throw new IllegalArgumentException("Unsupported argument for NEWARRAY operation [" + newarray.getType().toString() + ']');
      }
    }

    out.write(template.replace(MACROS_ADDRESS, sub));
    out.write(NEXT_LINE);
  }
}
