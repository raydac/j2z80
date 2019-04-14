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
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandDEFW extends AbstractAsmCommand {

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final byte[] result = new byte[asm.getArgs().length << 1];
    int index = 0;
    for (final String arg : asm.getArgs()) {
      final int value = new LightExpression(context, this, asm, arg).calculate();
      if (value < 0) {
        AsmAssertions.assertSignedShort(value);
      } else {
        AsmAssertions.assertUnsignedShort(value);
      }

      result[index++] = (byte) value;
      result[index++] = (byte) (value >>> 8);
    }
    return result;
  }

  @Override
  public String getName() {
    return "DEFW";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE_OR_MORE;
  }

  @Override
  public boolean isSpecialDirective() {
    return true;
  }
}
