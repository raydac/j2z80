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
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.IREM;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

// class to process IREM with code 112
public class Processor_IREM extends AbstractJvmCommandProcessor implements NeedsINTArithmeticManager, NeedsATHROWManager {
  private final String template;

  public Processor_IREM() {
    super();
    template = loadResourceFileAsString("IREM.a80").replace(MACROS_ADDRESS, SUB_INT_REM);
  }

  @Override
  public String getName() {
    return "IREM";
  }

  @Override
  public void process(final MethodTranslator classProcessor, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final IREM irem = (IREM) instruction;
    out.write(template);
    out.write(NEXT_LINE);
  }
}
