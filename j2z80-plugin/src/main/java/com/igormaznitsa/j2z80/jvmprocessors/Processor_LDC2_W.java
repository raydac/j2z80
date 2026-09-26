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
import com.igormaznitsa.j2z80.translator.utils.LongWords;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC2_W;

public class Processor_LDC2_W extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_LDC2_W() {
    super();
    this.template = loadResourceFileAsString("LDC2_W.a80");
  }

  @Override
  public String getName() {
    return "LDC2_W";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      final ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final LDC2_W ldc = (LDC2_W) instruction;
    final Constant constant = methodTranslator.getConstantPool().getConstant(ldc.getIndex());
    if (!(constant instanceof ConstantLong)) {
      throw new IllegalArgumentException(
          "Unsupported constant pool item found in LDC2_W [" + constant + ']');
    }
    final int value = LongWords.requireIntRange(((ConstantLong) constant).getBytes());
    out.write(this.template
        .replace("%high%", LongWords.highImmediate(value))
        .replace("%low%", LongWords.lowImmediate(value)));
    out.write(NEXT_LINE);
  }
}
