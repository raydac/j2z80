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
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandOUT extends AbstractAsmCommand {

  public AsmCommandOUT() {
    super();
    addCase("(C),A", (byte) 0xED, (byte) 0x79);
    addCase("(C),B", (byte) 0xED, (byte) 0x41);
    addCase("(C),C", (byte) 0xED, (byte) 0x49);
    addCase("(C),D", (byte) 0xED, (byte) 0x51);
    addCase("(C),E", (byte) 0xED, (byte) 0x59);
    addCase("(C),H", (byte) 0xED, (byte) 0x61);
    addCase("(C),L", (byte) 0xED, (byte) 0x69);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String port = asm.getArgs()[0];
    if ("(C)".equals(port)) {
      return getPatternCase(asm.getSignature());
    } else {
      final String rightPart = asm.getArgs()[1];
      Assertions.assertTrue("Port value must be in brakes [" + port + ']', isInBrakes(port));
      Assertions.assertTrue("The right operand must be A [" + rightPart + ']', "A".equals(rightPart));
      final int number = new LightExpression(context, this, asm, extractCalculatedPart(port)).calculate();
      AsmAssertions.assertUnsignedByte(number);
      return new byte[] {(byte) 0xD3, (byte) number};
    }
  }

  @Override
  public String getName() {
    return "OUT";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
