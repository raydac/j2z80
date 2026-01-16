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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;

// class to process GETFIELD with code 180
public class Processor_GETFIELD extends AbstractFieldProcessor {

  private final String template;

  public Processor_GETFIELD() {
    super();
    template = loadResourceFileAsString("GETFIELD.a80");
  }

  @Override
  public String getName() {
    return "GETFIELD";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      final ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final GETFIELD getfield = (GETFIELD) instruction;

    if (!processBootstrapClassCall(methodTranslator, getfield, bootstrapClassLoader, out)) {

      final ConstantPoolGen const_pool = methodTranslator.getConstantPool();
      final ObjectType objType = (ObjectType) getfield.getReferenceType(const_pool);

      final String labelOffset = LabelAndFrameUtils.makeLabelNameForFieldOffset(objType.getClassName(), getfield.getFieldName(const_pool), getfield.getFieldType(const_pool));

      out.write(template.replace(MACROS_ADDRESS, labelOffset));
      out.write(NEXT_LINE);
    }
  }
}
