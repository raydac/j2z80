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

public class AsmCommandADD extends AbstractAsmCommand {
  public AsmCommandADD() {
    super();
    addCase("A,A", (byte) 0x87);
    addCase("A,B", (byte) 0x80);
    addCase("A,C", (byte) 0x81);
    addCase("A,D", (byte) 0x82);
    addCase("A,E", (byte) 0x83);
    addCase("A,H", (byte) 0x84);
    addCase("A,L", (byte) 0x85);
    addCase("A,(HL)", (byte) 0x86);
    addCase("HL,BC", (byte) 0x09);
    addCase("HL,DE", (byte) 0x19);
    addCase("HL,HL", (byte) 0x29);
    addCase("HL,SP", (byte) 0x39);
    addCase("IX,BC", (byte) 0xDD, (byte) 0x09);
    addCase("IX,DE", (byte) 0xDD, (byte) 0x19);
    addCase("IX,IX", (byte) 0xDD, (byte) 0x29);
    addCase("IX,SP", (byte) 0xDD, (byte) 0x39);
    addCase("IY,BC", (byte) 0xFD, (byte) 0x09);
    addCase("IY,DE", (byte) 0xFD, (byte) 0x19);
    addCase("IY,IY", (byte) 0xFD, (byte) 0x29);
    addCase("IY,SP", (byte) 0xFD, (byte) 0x39);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String arg0 = asm.getArgs()[0];
    final String arg1 = asm.getArgs()[1];

    if ("A".equals(arg0)) {
      if (doesNeedCalculation(arg1)) {
        int number;
        if (!isRegisterName(arg1)) {
          if (isIndexRegisterReference(arg1)) {
            number = new LightExpression(context, this, asm, extractCalculatedPart(arg1)).calculate();
            AsmAssertions.assertUnsignedByte(number);
            return arg1.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0x86, (byte) number} :
                new byte[] {(byte) 0xFD, (byte) 0x86, (byte) number};
          } else {
            number = new LightExpression(context, this, asm, arg1).calculate();
            AsmAssertions.assertUnsignedByte(number);
            return new byte[] {(byte) 0xC6, (byte) number};
          }
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "ADD";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }

}
