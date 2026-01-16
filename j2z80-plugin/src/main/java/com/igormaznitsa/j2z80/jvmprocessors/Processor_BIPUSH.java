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
import java.util.Locale;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

// class to process BIPUSH with code 16
public class Processor_BIPUSH extends AbstractJvmCommandProcessor {
  private final String template;

  public Processor_BIPUSH() {
    super();
    template = loadResourceFileAsString("BIPUSH.a80");
  }

  @Override
  public String getName() {
    return "BIPUSH";
  }

  @Override
  public void process(final MethodTranslator classProcessor, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final BIPUSH bipush = (BIPUSH) instruction;

    final byte byteValue = bipush.getValue().byteValue();

    out.write(template.replace(MACROS_VALUE, "#" + Integer.toHexString(((int) byteValue) & 0xFFFF).toUpperCase(Locale.ENGLISH)));
    out.write(NEXT_LINE);
  }
}
