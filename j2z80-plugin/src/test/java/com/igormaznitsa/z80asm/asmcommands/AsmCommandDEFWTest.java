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

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsmCommandDEFWTest {

  final AbstractAsmCommand defwCommand = AbstractAsmCommand.findCommandForName("DEFW");

  @Test
  public void testMachineCodeGeneration() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("defw #0102,#0304,#0506,-34");
    final byte[] generated = defwCommand.makeMachineCode(mockContext, parsedLine);
    assertTrue(Arrays.equals(new byte[] {2, 1, 4, 3, 6, 5, -34, -1}, generated));
  }

  @Test
  public void testLabelUsageInDefinition() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    when(mockContext.findLabelAddress("SOME_LABEL")).thenReturn(Integer.valueOf(0x1234));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("defw SOME_LABEL");
    final byte[] generated = defwCommand.makeMachineCode(mockContext, parsedLine);
    assertTrue(Arrays.equals(new byte[] {0x34, 0x12}, generated));
  }
}
