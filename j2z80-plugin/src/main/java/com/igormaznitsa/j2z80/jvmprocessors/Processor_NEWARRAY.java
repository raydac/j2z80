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
