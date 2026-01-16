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

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;

// class to process INSTANCEOF with code 193
public class Processor_INSTANCEOF extends AbstractJvmCommandProcessor implements NeedsMemoryManager, NeedsInstanceofManager {
  private final String template;

  public Processor_INSTANCEOF() {
    super();
    template = loadResourceFileAsString("INSTANCEOF.a80");
  }

  @Override
  public String getName() {
    return "INSTANCEOF";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final INSTANCEOF instof = (INSTANCEOF) instruction;

    final ObjectType objectType = instof.getLoadClassType(methodTranslator.getConstantPool());
    final ClassID targetClassID = new ClassID(objectType.getClassName());

    methodTranslator.getTranslatorContext().registerClassForCastCheck(targetClassID);

    out.write(template.replace(MACROS_ID, LabelAndFrameUtils.makeLabelForClassID(targetClassID)));
    out.write(NEXT_LINE);
  }
}
