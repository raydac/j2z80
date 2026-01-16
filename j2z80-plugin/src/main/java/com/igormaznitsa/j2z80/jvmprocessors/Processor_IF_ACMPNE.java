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
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.IF_ACMPNE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

// class to process IF_ACMPNE with code 166
public class Processor_IF_ACMPNE extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_IF_ACMPNE() {
    super();
    template = loadResourceFileAsString("IF_ACMPNE.a80");
  }

  @Override
  public String getName() {
    return "IF_ACMPNE";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final IF_ACMPNE ifacmpne = (IF_ACMPNE) instruction;
    final String label = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), ((BranchHandle) handle).getTarget().getPosition());
    out.write(template.replace(MACROS_ADDRESS, label));
    out.write(NEXT_LINE);
  }
}
