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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;

import java.io.IOException;
import java.io.Writer;

// class to process GETSTATIC with code 178
public class Processor_GETSTATIC extends AbstractFieldProcessor {

  private final String template;

  public Processor_GETSTATIC() {
    super();
    template = loadResourceFileAsString("GETSTATIC.a80");
  }

  @Override
  public String getName() {
    return "GETSTATIC";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final GETSTATIC ins = (GETSTATIC) instruction;

    if (!processBootClassCall(methodTranslator, ins, out)) {
      final ConstantPoolGen cpool = methodTranslator.getConstantPool();
      final ObjectType obj = (ObjectType) ins.getReferenceType(cpool);
      final String address = LabelAndFrameUtils.makeLabelNameForField(obj.getClassName(), ins.getFieldName(cpool), ins.getFieldType(cpool));

      out.write(template.replace(MACROS_ADDRESS, address));
      out.write(NEXT_LINE);
    }
  }
}
