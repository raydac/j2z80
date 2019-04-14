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

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINTArithmeticManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import org.apache.bcel.generic.IMUL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import java.io.IOException;
import java.io.Writer;

// class to process IMUL with code 104
public class Processor_IMUL extends AbstractJvmCommandProcessor implements NeedsINTArithmeticManager, NeedsATHROWManager {
  private final String template;

  public Processor_IMUL() {
    super();
    template = loadResourceFileAsString("IMUL.a80").replace(MACROS_ADDRESS, SUB_INT_MUL);
  }

  @Override
  public String getName() {
    return "IMUL";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final IMUL imul = (IMUL) instruction;
    out.write(template);
    out.write(NEXT_LINE);
  }
}
