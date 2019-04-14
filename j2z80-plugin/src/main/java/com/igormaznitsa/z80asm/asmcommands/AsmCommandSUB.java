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

public class AsmCommandSUB extends AbstractAsmCommand {

  public AsmCommandSUB() {
    super();
    addCase("B", (byte) 0x90);
    addCase("C", (byte) 0x91);
    addCase("D", (byte) 0x92);
    addCase("E", (byte) 0x93);
    addCase("H", (byte) 0x94);
    addCase("L", (byte) 0x95);
    addCase("(HL)", (byte) 0x96);
    addCase("A", (byte) 0x97);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (doesNeedCalculation(arg)) {
      if (!isRegisterName(arg)) {
        int number;
        if (isIndexRegisterReference(arg)) {
          number = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
          AsmAssertions.assertSignedByte(number);
          return arg.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0x96, (byte) number} :
              new byte[] {(byte) 0xFD, (byte) 0x96, (byte) number};
        } else {
          number = new LightExpression(context, this, asm, arg).calculate();
          AsmAssertions.assertSignedByte(number);
          return new byte[] {(byte) 0xD6, (byte) number};
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "SUB";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
