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

import com.igormaznitsa.z80asm.AsmTranslator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AsmCommandENTTest {
  static final AbstractAsmCommand entCommand = AbstractAsmCommand.findCommandForName("ENT");

  @Test
  public void testMachineCode() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("label : ent $+1000");
    final byte[] generated = entCommand.makeMachineCode(mockContext, parsedLine);
    assertEquals(0, generated.length);
    verify(mockContext).setEntryPoint(2100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMachineCode_negativeAddress() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    final ParsedAsmLine parsedLine = new ParsedAsmLine("ent -1");
    entCommand.makeMachineCode(mockContext, parsedLine);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMachineCode_tooBigAddress() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    final ParsedAsmLine parsedLine = new ParsedAsmLine("ent #FFFF + 1");
    entCommand.makeMachineCode(mockContext, parsedLine);
  }
}
