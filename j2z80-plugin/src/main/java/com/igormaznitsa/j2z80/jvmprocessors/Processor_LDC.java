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
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;

// class to process LDC with code 18
public class Processor_LDC extends AbstractJvmCommandProcessor {

  private final String template;

  public Processor_LDC() {
    super();
    this.template = loadResourceFileAsString("LDC.a80");
  }

  @Override
  public String getName() {
    return "LDC";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final LDC ldc = (LDC) instruction;
    final int index = ldc.getIndex();
    final Constant cpConstant = methodTranslator.getConstantPool().getConstant(index);

    final String stringValue;
    if (cpConstant instanceof ConstantInteger) {
      final ConstantInteger constInt = (ConstantInteger) cpConstant;
      final int value = constInt.getBytes();
      stringValue = Integer.toString(value);
    } else if (cpConstant instanceof ConstantUtf8 || cpConstant instanceof ConstantString) {
      stringValue = methodTranslator.registerUsedConstantPoolItem(index);
    } else {
      methodTranslator.getTranslatorContext().getLogger()
          .logError("Unsupported constant pool element has been detected: " + cpConstant);
      throw new IllegalArgumentException(
          "Unsupported constant pool item found in a LDC instruction: " + cpConstant);
    }

    out.write(template.replace(MACROS_ADDRESS, stringValue));
    out.write(NEXT_LINE);
  }
}
