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
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsmCommandDEFSTest {
    final AbstractAsmCommand defsCommand = AbstractAsmCommand.findCommandForName("DEFS");
    
    @Test
    public void testMachineCodeGeneration() {
       final AsmTranslator mockContext = mock(AsmTranslator.class);
        when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
        final ParsedAsmLine parsedLine = new ParsedAsmLine("defs 2000+8000");
        final byte [] generated = defsCommand.makeMachineCode(mockContext, parsedLine);
        assertEquals(10000, generated.length);
    }
}