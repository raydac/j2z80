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

public class AsmCommandINC extends AbstractAsmCommand {

  public AsmCommandINC() {
    super();
    addCase("A", (byte) 0x3C);
    addCase("B", (byte) 0x04);
    addCase("C", (byte) 0x0C);
    addCase("D", (byte) 0x14);
    addCase("E", (byte) 0x1C);
    addCase("H", (byte) 0x24);
    addCase("L", (byte) 0x2C);
    addCase("(HL)", (byte) 0x34);
    addCase("BC", (byte) 0x03);
    addCase("DE", (byte) 0x13);
    addCase("HL", (byte) 0x23);
    addCase("SP", (byte) 0x33);
    addCase("IX", (byte) 0xDD, (byte) 0x23);
    addCase("IY", (byte) 0xFD, (byte) 0x23);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (isIndexRegisterReference(arg)) {
      final byte prefix = arg.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
      AsmAssertions.assertSignedByte(offset);
      return new byte[] {prefix, (byte) 0x34, (byte) offset};
    } else {
      return getPatternCase(asm.getSignature());
    }
  }

  @Override
  public String getName() {
    return "INC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }


}
