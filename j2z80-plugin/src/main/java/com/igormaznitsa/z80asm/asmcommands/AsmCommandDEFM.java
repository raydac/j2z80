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

public class AsmCommandDEFM extends AbstractAsmCommand {

  private static String unescape(final String str) {
    if (str.isEmpty()) {
      return str;
    }

    Assert.assertTrue("DEFM takes a string as argument [" + str + ']', str.startsWith("\""));
    Assert.assertTrue("String must be closed [" + str + ']', str.endsWith("\""));
    return str.substring(1, str.length() - 1);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String str = unescape(asm.getArgs()[0]);
    return str.getBytes();
  }

  @Override
  public String getName() {
    return "DEFM";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }

  @Override
  public boolean isSpecialDirective() {
    return true;
  }

}
