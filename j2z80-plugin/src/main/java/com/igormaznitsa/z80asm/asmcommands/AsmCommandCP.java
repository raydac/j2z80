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

public class AsmCommandCP extends AbstractAsmCommand {

  public AsmCommandCP() {
    super();
    addCase("B", (byte) 0xB8);
    addCase("C", (byte) 0xB9);
    addCase("D", (byte) 0xBA);
    addCase("E", (byte) 0xBB);
    addCase("H", (byte) 0xBC);
    addCase("L", (byte) 0xBD);
    addCase("(HL)", (byte) 0xBE);
    addCase("A", (byte) 0xBF);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (doesNeedCalculation(arg)) {
      if (!isRegisterName(arg)) {
        int number;
        if (isIndexRegisterReference(arg)) {
          number = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
          Assert.assertSignedByte(number);
          return arg.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0xBE, (byte) number}
              : new byte[] {(byte) 0xFD, (byte) 0xBE, (byte) number};
        } else {
          number = new LightExpression(context, this, asm, arg).calculate();
          Assert.assertSignedByte(number);
          return new byte[] {(byte) 0xFE, (byte) number};
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "CP";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
