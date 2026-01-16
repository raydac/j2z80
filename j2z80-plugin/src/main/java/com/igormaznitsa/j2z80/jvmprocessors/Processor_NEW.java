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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;

// class to process NEW with code 187
public class Processor_NEW extends AbstractJvmCommandProcessor implements NeedsMemoryManager {
  private final String template;

  public Processor_NEW() {
    super();
    template = loadResourceFileAsString("NEW.a80").replace(MACROS_ADDRESS, SUB_ALLOCATE_OBJECT);
  }

  @Override
  public String getName() {
    return "NEW";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final NEW newins = (NEW) instruction;
    final ObjectType type = newins.getLoadClassType(methodTranslator.getConstantPool());
    final String className = type.getClassName();

    final Integer classID = methodTranslator.getTranslatorContext().getClassContext().findClassUID(new ClassID(className));

    Assertions.assertNotNull("Class ID must not be null [" + className + ']', classID);

    final String classInfoLabel = LabelAndFrameUtils.makeLabelForClassSizeInfo(type);

    out.write(template.replace(MACROS_VALUE, classInfoLabel).replace(MACROS_ID, Integer.toString(classID)));
    out.write(NEXT_LINE);
  }
}
