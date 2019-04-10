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

public class AsmCommandSBC extends AbstractAsmCommand {

  public AsmCommandSBC() {
    super();
    addCase("A,A", (byte) 0x9F);
    addCase("A,B", (byte) 0x98);
    addCase("A,C", (byte) 0x99);
    addCase("A,D", (byte) 0x9A);
    addCase("A,E", (byte) 0x9B);
    addCase("A,H", (byte) 0x9C);
    addCase("A,L", (byte) 0x9D);
    addCase("A,(HL)", (byte) 0x9E);
    addCase("HL,BC", (byte) 0xED, (byte) 0x42);
    addCase("HL,DE", (byte) 0xED, (byte) 0x52);
    addCase("HL,HL", (byte) 0xED, (byte) 0x62);
    addCase("HL,SP", (byte) 0xED, (byte) 0x72);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String argLeft = asm.getArgs()[0];
    final String argRight = asm.getArgs()[1];

    if ("A".equals(argLeft)) {

      if (doesNeedCalculation(argRight)) {
        if (!isRegisterName(argRight)) {
          int number;
          if (isIndexRegisterReference(argRight)) {
            number = new LightExpression(context, this, asm, extractCalculatedPart(argRight)).calculate();
            Assert.assertUnsignedByte(number);
            return argRight.startsWith("(IX") ? new byte[] {(byte) 0xDD, (byte) 0x9E, (byte) number}
                : new byte[] {(byte) 0xFD, (byte) 0x9E, (byte) number};
          } else {
            number = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
            Assert.assertUnsignedByte(number);
            return new byte[] {(byte) 0xDE, (byte) number};
          }
        }
      }
    }
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "SBC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
