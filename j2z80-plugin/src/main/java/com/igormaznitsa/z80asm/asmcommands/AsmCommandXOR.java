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

public class AsmCommandXOR extends AbstractAsmCommand {

  public AsmCommandXOR() {
    super();
    addCase("B", (byte) 0xA8);
    addCase("C", (byte) 0xA9);
    addCase("D", (byte) 0xAA);
    addCase("E", (byte) 0xAB);
    addCase("H", (byte) 0xAC);
    addCase("L", (byte) 0xAD);
    addCase("(HL)", (byte) 0xAE);
    addCase("A", (byte) 0xAF);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (doesNeedCalculation(arg)) {
      if (!isRegisterName(arg)) {
        int number;
        if (isIndexRegisterReference(arg)) {
          number = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
          Assert.assertUnsignedByte(number);
          return arg.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0xAE, (byte) number} :
              new byte[] {(byte) 0xFD, (byte) 0xAE, (byte) number};
        } else {
          number = new LightExpression(context, this, asm, arg).calculate();
          Assert.assertUnsignedByte(number);
          return new byte[] {(byte) 0xEE, (byte) number};
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "XOR";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
