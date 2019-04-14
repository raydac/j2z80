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

public class AsmCommandCALL extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    if (asm.getArgs().length == 1) {
      final int address = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
      AsmAssertions.assertAddress(address);
      return new byte[] {(byte) 0xCD, (byte) address, (byte) (address >>> 8)};
    } else {
      final String flag = asm.getArgs()[0];
      final int address = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
      AsmAssertions.assertAddress(address);
      byte command = 0;

      if ("NZ".equals(flag)) {
        command = (byte) 0xC4;
      } else if ("Z".equals(flag)) {
        command = (byte) 0xCC;
      } else if ("NC".equals(flag)) {
        command = (byte) 0xD4;

      } else if ("C".equals(flag)) {
        command = (byte) 0xDC;

      } else if ("PO".equals(flag)) {
        command = (byte) 0xE4;

      } else if ("PE".equals(flag)) {
        command = (byte) 0xEC;

      } else if ("P".equals(flag)) {
        command = (byte) 0xF4;

      } else if ("M".equals(flag)) {
        command = (byte) 0xFC;

      }

      Assertions.assertFalse("Unsupported flag for CALL command [" + flag + ']', command == 0);

      return new byte[] {command, (byte) address, (byte) (address >>> 8)};
    }
  }

  @Override
  public String getName() {
    return "CALL";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE_OR_TWO;
  }
}
