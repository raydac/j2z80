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

public class AsmCommandJP extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
    if (asm.getArgs().length == 1) {
      if (isInBrakes(asm.getArgs()[0])) {
        final String arg = asm.getArgs()[0];

        byte[] result = null;

        if ("(HL)".equals(arg)) {
          result = new byte[] {(byte) 0xE9};
        } else if ("(IX)".equals(arg)) {
          result = new byte[] {(byte) 0xDD, (byte) 0xE9};
        } else if ("(IY)".equals(arg)) {
          result = new byte[] {(byte) 0xFD, (byte) 0xE9};
        }

        Assertions.assertNotNull("Wrong register usage for JP command [" + arg + ']', result);

        return result;
      } else {
        final int address = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
        AsmAssertions.assertAddress(address);
        return new byte[] {(byte) 0xC3, (byte) address, (byte) (address >>> 8)};
      }
    } else {
      final String flag = asm.getArgs()[0];
      final int address = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
      AsmAssertions.assertAddress(address);
      byte command = 0;

      if ("NZ".equals(flag)) {
        command = (byte) 0xC2;
      } else if ("Z".equals(flag)) {
        command = (byte) 0xCA;
      } else if ("NC".equals(flag)) {
        command = (byte) 0xD2;
      } else if ("C".equals(flag)) {
        command = (byte) 0xDA;
      } else if ("PO".equals(flag)) {
        command = (byte) 0xE2;
      } else if ("PE".equals(flag)) {
        command = (byte) 0xEA;
      } else if ("P".equals(flag)) {
        command = (byte) 0xF2;
      } else if ("M".equals(flag)) {
        command = (byte) 0xFA;
      }

      Assertions.assertFalse("Unsupported flag for JP command [" + flag + ']', command == 0);

      return new byte[] {command, (byte) address, (byte) (address >>> 8)};
    }
  }

  @Override
  public String getName() {
    return "JP";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE_OR_TWO;
  }
}
