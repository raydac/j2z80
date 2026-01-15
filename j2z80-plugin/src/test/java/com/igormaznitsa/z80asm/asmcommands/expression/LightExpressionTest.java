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
package com.igormaznitsa.z80asm.asmcommands.expression;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.LocalLabelExpectant;
import com.igormaznitsa.z80asm.expression.LightExpression;
import org.junit.Test;

public class LightExpressionTest {
  @Test
  public void testCalculation() {
    final AsmTranslator context = mock(AsmTranslator.class);
    when(context.findLabelAddress("LABEL")).thenReturn(Integer.valueOf(0xFFED));
    when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
    final LightExpression expression = new LightExpression(context, null, null, "-#FF+\"BA\"-%101001001+LABEL-$");
    assertEquals(((-255) + ((66 << 8) | 65) - 329 + 65517 - 4660), expression.calculate());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownLabel() {
    final AsmTranslator context = mock(AsmTranslator.class);
    when(context.findLabelAddress("LABEL")).thenReturn(null);
    when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
    new LightExpression(context, null, null, "-#FF+\"BA\"-%101001001+LABEL-$").calculate();
  }

  @Test
  public void testUnknownLocalLabel() {
    final AsmTranslator context = mock(AsmTranslator.class);
    when(context.findLabelAddress("@LABEL")).thenReturn(null);
    when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
    new LightExpression(context, null, null, "$-@LABEL").calculate();
    verify(context).registerLocalLabelExpectant(eq("@LABEL"), any(LocalLabelExpectant.class));
  }

  @Test
  public void testBigLabel() {
    final AsmTranslator context = mock(AsmTranslator.class);
    when(context.findLabelAddress("java.lang.System.out#Ljava/io/PrintStream;")).thenReturn(Integer.valueOf(0x1000));
    assertEquals(0x1000L, new LightExpression(context, null, null, "java.lang.System.out#Ljava/io/PrintStream;").calculate());
  }
}
