/*
 * Copyright 2012-2026 Igor Maznitsa.
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
import com.igormaznitsa.j2z80.translator.utils.LongWords;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LCONST;

public class Processor_LCONST extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_LCONST() {
    super();
    this.template = loadResourceFileAsString("LCONST.a80");
  }

  @Override
  public String getName() {
    return "LCONST";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      final ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final int value = LongWords.requireIntRange(((LCONST) instruction).getValue().longValue());
    out.write(this.template
        .replace("%high%", LongWords.highImmediate(value))
        .replace("%low%", LongWords.lowImmediate(value)));
    out.write(NEXT_LINE);
  }
}
