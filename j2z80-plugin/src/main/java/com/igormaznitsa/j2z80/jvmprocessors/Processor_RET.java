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
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.RET;

import java.io.IOException;
import java.io.Writer;

// class to process RET with code 169
public class Processor_RET extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_RET() {
    super();
    template = loadResourceFileAsString("RET.a80");
  }

  @Override
  public String getName() {
    return "RET";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final RET ret = (RET) instruction;
    out.write(template.replace(MACROS_INDEX, Integer.toString(ret.getIndex() << 1)));
    out.write(NEXT_LINE);
  }
}
