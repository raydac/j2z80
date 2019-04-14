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

public class AsmCommandRR extends AbstractAsmCommand {

  public AsmCommandRR() {
    super();
    addCase("A", (byte) 0xCB, (byte) 0x1F);
    addCase("B", (byte) 0xCB, (byte) 0x18);
    addCase("C", (byte) 0xCB, (byte) 0x19);
    addCase("D", (byte) 0xCB, (byte) 0x1A);
    addCase("E", (byte) 0xCB, (byte) 0x1B);
    addCase("H", (byte) 0xCB, (byte) 0x1C);
    addCase("L", (byte) 0xCB, (byte) 0x1D);
    addCase("(HL)", (byte) 0xCB, (byte) 0x1E);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];
    if (isIndexRegisterReference(arg)) {
      final byte prefix = arg.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
      AsmAssertions.assertSignedByte(offset);
      return new byte[] {prefix, (byte) 0xCB, (byte) offset, (byte) 0x1E};
    } else {
      return getPatternCase(asm.getSignature());
    }
  }

  @Override
  public String getName() {
    return "RR";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
