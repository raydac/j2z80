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

public class AsmCommandOR extends AbstractAsmCommand {

  public AsmCommandOR() {
    super();
    addCase("B", (byte) 0xB0);
    addCase("C", (byte) 0xB1);
    addCase("D", (byte) 0xB2);
    addCase("E", (byte) 0xB3);
    addCase("H", (byte) 0xB4);
    addCase("L", (byte) 0xB5);
    addCase("(HL)", (byte) 0xB6);
    addCase("A", (byte) 0xB7);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (doesNeedCalculation(arg)) {
      if (!isRegisterName(arg)) {
        int number;
        if (isIndexRegisterReference(arg)) {
          number = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
          AsmAssertions.assertUnsignedByte(number);
          return arg.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0xB6, (byte) number}
              : new byte[] {(byte) 0xFD, (byte) 0xB6, (byte) number};
        } else {
          number = new LightExpression(context, this, asm, arg).calculate();
          AsmAssertions.assertUnsignedByte(number);
          return new byte[] {(byte) 0xF6, (byte) number};
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "OR";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
