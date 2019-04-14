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

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandSBC extends AbstractAsmCommand {

  public AsmCommandSBC() {
    super();
    addCase("A,A", (byte) 0x9F);
    addCase("A,B", (byte) 0x98);
    addCase("A,C", (byte) 0x99);
    addCase("A,D", (byte) 0x9A);
    addCase("A,E", (byte) 0x9B);
    addCase("A,H", (byte) 0x9C);
    addCase("A,L", (byte) 0x9D);
    addCase("A,(HL)", (byte) 0x9E);
    addCase("HL,BC", (byte) 0xED, (byte) 0x42);
    addCase("HL,DE", (byte) 0xED, (byte) 0x52);
    addCase("HL,HL", (byte) 0xED, (byte) 0x62);
    addCase("HL,SP", (byte) 0xED, (byte) 0x72);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String argLeft = asm.getArgs()[0];
    final String argRight = asm.getArgs()[1];

    if ("A".equals(argLeft)) {

      if (doesNeedCalculation(argRight)) {
        if (!isRegisterName(argRight)) {
          int number;
          if (isIndexRegisterReference(argRight)) {
            number = new LightExpression(context, this, asm, extractCalculatedPart(argRight)).calculate();
            Assert.assertUnsignedByte(number);
            return argRight.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0x9E, (byte) number}
                : new byte[] {(byte) 0xFD, (byte) 0x9E, (byte) number};
          } else {
            number = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
            Assert.assertUnsignedByte(number);
            return new byte[] {(byte) 0xDE, (byte) number};
          }
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "SBC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
