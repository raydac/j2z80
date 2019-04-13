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

package com.igormaznitsa.z80asm.asmcommands.expression;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.LocalLabelExpectant;
import com.igormaznitsa.z80asm.expression.LightExpression;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
