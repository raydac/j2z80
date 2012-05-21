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
package com.igormaznitsa.z80asm.expression;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LightExpressionTest {
    final AsmTranslator MOCK_TRANSLATOR = mock(AsmTranslator.class);
    final AbstractAsmCommand MOCK_COMMAND = mock(AbstractAsmCommand.class);
    final ParsedAsmLine MOCK_PARSED_LINE = mock(ParsedAsmLine.class);

    @Test
    public void testLinkToNestedClassMethod() {
        final String label  = "com.igormaznitsa.j2z80test.Main.<init>#_ILcom/igormaznitsa/j2z80test/Main@AbstractTemplateGenII_V";
        final int address = 0xCAFE;
        when(MOCK_TRANSLATOR.findLabelAddress(eq(label))).thenReturn(Integer.valueOf(address));
        assertEquals("Must return the label address", address, new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, label).calculate());
    }
}
