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

    @Test
    public void testAddOperator() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "1+2+3+4+5").calculate();
        assertEquals((1+2+3+4+5), value);
    }

    @Test
    public void testSingleAddOperator() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "+5").calculate();
        assertEquals(5, value);
    }

    @Test
    public void testSubOperator() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "34-12-5-6").calculate();
        assertEquals((34-12-5-6), value);
    }

    @Test
    public void testSingleSubOperator() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "-12").calculate();
        assertEquals((-12), value);
    }

    @Test
    public void testExpressionWithSpaces() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "  -  4   +8 \t -  \n 9    ").calculate();
        assertEquals((-4+8-9), value);
    }

    @Test
    public void testStringDeclarationWithRegularChars(){
        final int value = new LightExpression(MOCK_TRANSLATOR,MOCK_COMMAND, MOCK_PARSED_LINE, "\"abcd\"").calculate();
        assertEquals("The value must be 'abcd'",((int)'a'<<24) | ((int)'b'<<16) | ((int)'c'<<8) | 'd', value);
    }

    @Test
    public void testSpecialCharInString() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "\"\\\\\"").calculate();
        assertEquals("The value must be '\\'", (int)'\\', value);
    }

    @Test
    public void testStringDeclarationWithSpecialChars(){
        final int value = new LightExpression(MOCK_TRANSLATOR,MOCK_COMMAND, MOCK_PARSED_LINE, "\"\b\t\n\r\"").calculate();
        assertEquals("The value must be '\b\t\n\r'",((int)'\b'<<24) | ((int)'\t'<<16) | ((int)'\n'<<8) | '\r', value);
    }

    @Test
    public void testHexNumberInExpression(){
        final int value = new LightExpression(MOCK_TRANSLATOR,MOCK_COMMAND, MOCK_PARSED_LINE, "#10+#20").calculate();
        assertEquals("The value must be 0x30",0x30, value);
    }

    @Test
    public void testBinaryNumberInExpression() {
        final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "10+%00110100100101-\"a\"").calculate();
        assertEquals((10+Integer.parseInt("00110100100101",2)-'a'), value);
    }
    
    @Test
    public void testComplexExpression(){
        final int value = new LightExpression(MOCK_TRANSLATOR,MOCK_COMMAND, MOCK_PARSED_LINE, "344-11+\"\\\\\"+#23").calculate();
        assertEquals((344-11+'\\'+0x23), value);
    }
}
