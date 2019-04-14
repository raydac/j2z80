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
