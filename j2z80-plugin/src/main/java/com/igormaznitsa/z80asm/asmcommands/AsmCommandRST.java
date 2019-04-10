/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
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
