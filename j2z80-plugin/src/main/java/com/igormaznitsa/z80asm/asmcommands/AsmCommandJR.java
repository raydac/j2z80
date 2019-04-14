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

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandJR extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    if (asm.getArgs().length == 1) {
      final int address = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
      final int offset = calculateAddressOffset(address, context.getPC());
      return new byte[] {(byte) 0x18, (byte) offset};
    } else {
      final String flag = asm.getArgs()[0];
      final int address = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
      final int offset = calculateAddressOffset(address, context.getPC());
      byte command = 0;

      if ("NZ".equals(flag)) {
        command = (byte) 0x20;
      } else if ("Z".equals(flag)) {
        command = (byte) 0x28;
      } else if ("NC".equals(flag)) {
        command = (byte) 0x30;

      } else if ("C".equals(flag)) {
        command = (byte) 0x38;
      }

      Assertions.assertFalse("Unsupported flag for JR command [" + flag + ']', command == 0);

      return new byte[] {command, (byte) offset};
    }
  }

  @Override
  public String getName() {
    return "JR";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE_OR_TWO;
  }
}
