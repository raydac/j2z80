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
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

public class Processor_DSTORE extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_DSTORE() {
    super();
    this.template = loadResourceFileAsString("DSTORE.a80");
  }

  @Override
  public String getName() {
    return "DSTORE";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      final ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final int index = ((DSTORE) instruction).getIndex();
    out.write(this.template
        .replace("%high%", Integer.toString(prepareLocalVariableIndex(index + 1)))
        .replace(MACROS_INDEX, Integer.toString(prepareLocalVariableIndex(index))));
    out.write(NEXT_LINE);
  }
}
