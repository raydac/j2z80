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

public class AsmCommandDEC extends AbstractAsmCommand {

  public AsmCommandDEC() {
    super();
    addCase("A", (byte) 0x3D);
    addCase("B", (byte) 0x05);
    addCase("C", (byte) 0x0D);
    addCase("D", (byte) 0x15);
    addCase("E", (byte) 0x1D);
    addCase("H", (byte) 0x25);
    addCase("L", (byte) 0x2D);
    addCase("(HL)", (byte) 0x35);
    addCase("BC", (byte) 0x0B);
    addCase("DE", (byte) 0x1B);
    addCase("HL", (byte) 0x2B);
    addCase("SP", (byte) 0x3B);
    addCase("IX", (byte) 0xDD, (byte) 0x2B);
    addCase("IY", (byte) 0xFD, (byte) 0x2B);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String arg = asm.getArgs()[0];

    if (isIndexRegisterReference(arg)) {
      final byte prefix = arg.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
      Assert.assertSignedByte(offset);
      return new byte[] {prefix, (byte) 0x35, (byte) offset};
    } else {
      return getPatternCase(asm.getSignature());
    }
  }

  @Override
  public String getName() {
    return "DEC";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }

}
