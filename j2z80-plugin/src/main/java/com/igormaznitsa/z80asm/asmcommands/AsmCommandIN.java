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

public class AsmCommandIN extends AbstractAsmCommand {

  public AsmCommandIN() {
    super();
    addCase("A,(C)", (byte) 0xED, (byte) 0x78);
    addCase("B,(C)", (byte) 0xED, (byte) 0x40);
    addCase("C,(C)", (byte) 0xED, (byte) 0x48);
    addCase("D,(C)", (byte) 0xED, (byte) 0x50);
    addCase("E,(C)", (byte) 0xED, (byte) 0x58);
    addCase("H,(C)", (byte) 0xED, (byte) 0x60);
    addCase("L,(C)", (byte) 0xED, (byte) 0x68);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final String port = asm.getArgs()[1];
    if ("(C)".equals(port)) {
      return getPatternCase(asm.getSignature());
    } else {
      final String leftPart = asm.getArgs()[0];
      Assert.assertTrue("The port must be in brakes [" + port + ']', isInBrakes(port));
      Assert.assertTrue("The left part must be A [" + leftPart + ']', "A".equals(leftPart));
      final int number = new LightExpression(context, this, asm, extractCalculatedPart(port)).calculate();
      Assert.assertUnsignedByte(number);
      return new byte[] {(byte) 0xDB, (byte) number};
    }
  }

  @Override
  public String getName() {
    return "IN";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
