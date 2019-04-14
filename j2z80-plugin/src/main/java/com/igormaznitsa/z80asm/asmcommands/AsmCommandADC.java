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

public class AsmCommandADC extends AbstractAsmCommand {

  public AsmCommandADC() {
    super();
    addCase("A,A", (byte) 0x8F);
    addCase("A,B", (byte) 0x88);
    addCase("A,C", (byte) 0x89);
    addCase("A,D", (byte) 0x8A);
    addCase("A,E", (byte) 0x8B);
    addCase("A,H", (byte) 0x8C);
    addCase("A,L", (byte) 0x8D);
    addCase("A,(HL)", (byte) 0x8E);
    addCase("HL,BC", (byte) 0xED, (byte) 0x4A);
    addCase("HL,DE", (byte) 0xED, (byte) 0x5A);
    addCase("HL,HL", (byte) 0xED, (byte) 0x6A);
    addCase("HL,SP", (byte) 0xED, (byte) 0x7A);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String leftArg = asm.getArgs()[0];
    final String rightArg = asm.getArgs()[1];

    if ("A".equals(leftArg)) {
      if (doesNeedCalculation(rightArg)) {
        if (!isRegisterName(rightArg)) {
          int number;

          if (isIndexRegisterReference(rightArg)) {
            number = new LightExpression(context, this, asm, extractCalculatedPart(rightArg)).calculate();
            AsmAssertions.assertUnsignedByte(number);
            return rightArg.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0x8E, (byte) number}
                : new byte[] {(byte) 0xFD, (byte) 0x8E, (byte) number};
          } else {
            number = new LightExpression(context, this, asm, rightArg).calculate();
            AsmAssertions.assertUnsignedByte(number);
            return new byte[] {(byte) 0xCE, (byte) number};
          }
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "ADC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
