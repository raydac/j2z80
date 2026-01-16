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

import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MONITORENTER;

// class to process MONITORENTER with code 194
public class Processor_MONITORENTER extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_MONITORENTER() {
    super();
    template = loadResourceFileAsString("MONITORENTER.a80");
  }

  @Override
  public String getName() {
    return "MONITORENTER";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    methodTranslator.getTranslatorContext().getLogger().logWarning("A MONITORENTER instruction has been met");
    final MONITORENTER monitorenter = (MONITORENTER) instruction;
    out.write(template);
    out.write(NEXT_LINE);
  }
}
