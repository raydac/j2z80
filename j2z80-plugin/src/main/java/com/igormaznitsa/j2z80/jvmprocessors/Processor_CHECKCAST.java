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
import com.igormaznitsa.j2z80.api.additional.NeedsCheckcastManager;
import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;

// class to process CHECKCAST with code 192
public class Processor_CHECKCAST extends AbstractJvmCommandProcessor implements NeedsMemoryManager, NeedsCheckcastManager, NeedsInstanceofManager, NeedsATHROWManager {

  private final String template;

  public Processor_CHECKCAST() {
    super();
    template = loadResourceFileAsString("CHECKCAST.a80").replace(MACROS_ADDRESS, SUB_CHECKCAST);
  }

  @Override
  public String getName() {
    return "CHECKCAST";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final CHECKCAST checkcast = (CHECKCAST) instruction;

    final ObjectType type = checkcast.getLoadClassType(methodTranslator.getConstantPool());

    final ClassID castingClassId = new ClassID(type.getClassName());

    methodTranslator.getTranslatorContext().registerClassForCastCheck(castingClassId);

    out.write(template.replace(MACROS_ID, LabelAndFrameUtils.makeLabelForClassID(castingClassId)));
    out.write(NEXT_LINE);
  }
}
