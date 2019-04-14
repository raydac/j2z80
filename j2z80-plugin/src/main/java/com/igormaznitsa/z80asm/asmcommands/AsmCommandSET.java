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

public class AsmCommandSET extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    final int number = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
    final String register = asm.getArgs()[1];

    Assert.assertZero("Bit number is outbound [" + number + ']', number & ~0x7);

    final int basecode = 0xC0 + (number << 3);
    if (isIndexRegisterReference(register)) {
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(register)).calculate();
      Assert.assertSignedByte(offset);
      final byte prefix = register.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
      return new byte[] {prefix, (byte) 0xCB, (byte) offset, (byte) (basecode + 6)};
    } else {
      final int registerIndex = getRegisterOrder(register);
      return new byte[] {(byte) 0xCB, (byte) (basecode + registerIndex)};
    }
  }

  @Override
  public String getName() {
    return "SET";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
