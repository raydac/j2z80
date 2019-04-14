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
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;

import java.io.IOException;
import java.io.Writer;

// class to process LDC with code 18
public class Processor_LDC extends AbstractJvmCommandProcessor {

  private final String template;

  public Processor_LDC() {
    super();
    template = loadResourceFileAsString("LDC.a80");
  }

  @Override
  public String getName() {
    return "LDC";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final LDC ldc = (LDC) instruction;

    final int index = ldc.getIndex();

    String strvalue;
    final Constant cp_constant = methodTranslator.getConstantPool().getConstant(index);

    if (cp_constant instanceof ConstantInteger) {
      final ConstantInteger constInt = (ConstantInteger) cp_constant;
      final int value = constInt.getBytes();
      strvalue = Integer.toString(value);
    } else if (cp_constant instanceof ConstantUtf8 || cp_constant instanceof ConstantString) {
      strvalue = methodTranslator.registerUsedConstantPoolItem(index);
    } else {
      methodTranslator.getTranslatorContext().getLogger().logError("Unsupported constant pool element has been detected [" + cp_constant.toString() + ']');
      throw new IllegalArgumentException("Unsupported constant pool item detected in LDC instruction [" + cp_constant.toString() + ']');
    }

    out.write(template.replace(MACROS_ADDRESS, strvalue));
    out.write(NEXT_LINE);
  }
}
