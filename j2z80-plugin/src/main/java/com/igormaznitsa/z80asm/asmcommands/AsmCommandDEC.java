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
package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.j2z80.translator.utils.AsmAssertions;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandDEC extends AbstractAsmCommand {

  public AsmCommandDEC() {
    super();
    addCase("A", (byte) 0x3D);
    addCase("B", (byte) 0x05);
    addCase("C", (byte) 0x0D);
    addCase("D", (byte) 0x15);
    addCase("E", (byte) 0x1D);
    addCase("H", (byte) 0x25);
    addCase("L", (byte) 0x2D);
    addCase("(HL)", (byte) 0x35);
    addCase("BC", (byte) 0x0B);
    addCase("DE", (byte) 0x1B);
    addCase("HL", (byte) 0x2B);
    addCase("SP", (byte) 0x3B);
    addCase("IX", (byte) 0xDD, (byte) 0x2B);
    addCase("IY", (byte) 0xFD, (byte) 0x2B);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (isIndexRegisterReference(arg)) {
      final byte prefix = arg.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
      AsmAssertions.assertSignedByte(offset);
      return new byte[] {prefix, (byte) 0x35, (byte) offset};
    } else {
      return getPatternCase(asm.getSignature());
    }
  }

  @Override
  public String getName() {
    return "DEC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }

}
