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

public class AsmCommandRST extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(AsmTranslator context, final ParsedAsmLine asm) {

    final int number = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();

    byte[] result = null;

    switch (number) {
      case 0:
        result = new byte[] {(byte) 0xC7};
        break;
      case 8:
        result = new byte[] {(byte) 0xCF};
        break;
      case 16:
        result = new byte[] {(byte) 0xD7};
        break;
      case 24:
        result = new byte[] {(byte) 0xDF};
        break;
      case 32:
        result = new byte[] {(byte) 0xE7};
        break;
      case 40:
        result = new byte[] {(byte) 0xEF};
        break;
      case 48:
        result = new byte[] {(byte) 0xF7};
        break;
      case 56:
        result = new byte[] {(byte) 0xFF};
        break;
    }

    Assert.assertNotNull("Wrong RST argument [" + asm.getArgs()[0] + ']', result);

    return result;
  }

  @Override
  public String getName() {
    return "RST";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }
}
