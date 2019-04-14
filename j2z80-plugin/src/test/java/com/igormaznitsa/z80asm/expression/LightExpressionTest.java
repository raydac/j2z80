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
package com.igormaznitsa.z80asm.expression;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LightExpressionTest {
  final AsmTranslator MOCK_TRANSLATOR = mock(AsmTranslator.class);
  final AbstractAsmCommand MOCK_COMMAND = mock(AbstractAsmCommand.class);
  final ParsedAsmLine MOCK_PARSED_LINE = mock(ParsedAsmLine.class);

  @Test
  public void testLinkToNestedClassMethod() {
    final String label = "com.igormaznitsa.j2z80test.Main.<init>#_ILcom/igormaznitsa/j2z80test/Main@AbstractTemplateGenII_V";
    final int address = 0xCAFE;
    when(MOCK_TRANSLATOR.findLabelAddress(eq(label))).thenReturn(Integer.valueOf(address));
    assertEquals("Must return the label address", address, new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, label).calculate());
  }

  @Test
  public void testAddOperator() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "1+2+3+4+5").calculate();
    assertEquals((1 + 2 + 3 + 4 + 5), value);
  }

  @Test
  public void testSingleAddOperator() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "+5").calculate();
    assertEquals(5, value);
  }

  @Test
  public void testSubOperator() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "34-12-5-6").calculate();
    assertEquals((34 - 12 - 5 - 6), value);
  }

  @Test
  public void testSingleSubOperator() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "-12").calculate();
    assertEquals((-12), value);
  }

  @Test
  public void testExpressionWithSpaces() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "  -  4   +8 \t -  \n 9    ").calculate();
    assertEquals((-4 + 8 - 9), value);
  }

  @Test
  public void testStringDeclarationWithRegularChars() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "\"abcd\"").calculate();
    assertEquals("The value must be 'abcd'", ((int) 'a' << 24) | ((int) 'b' << 16) | ((int) 'c' << 8) | 'd', value);
  }

  @Test
  public void testSpecialCharInString() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "\"\\\\\"").calculate();
    assertEquals("The value must be '\\'", (int) '\\', value);
  }

  @Test
  public void testStringDeclarationWithSpecialChars() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "\"\b\t\n\r\"").calculate();
    assertEquals("The value must be '\b\t\n\r'", ((int) '\b' << 24) | ((int) '\t' << 16) | ((int) '\n' << 8) | '\r', value);
  }

  @Test
  public void testHexNumberInExpression() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "#10+#20").calculate();
    assertEquals("The value must be 0x30", 0x30, value);
  }

  @Test
  public void testBinaryNumberInExpression() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "10+%00110100100101-\"a\"").calculate();
    assertEquals((10 + Integer.parseInt("00110100100101", 2) - 'a'), value);
  }

  @Test
  public void testComplexExpression() {
    final int value = new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, "344-11+\"\\\\\"+#23").calculate();
    assertEquals((344 - 11 + '\\' + 0x23), value);
  }
}
