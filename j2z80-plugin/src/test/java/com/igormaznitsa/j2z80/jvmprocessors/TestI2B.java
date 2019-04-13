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

package com.igormaznitsa.j2z80.jvmprocessors;

import org.apache.bcel.generic.I2B;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestI2B extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution_Negative() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(I2B.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = -348;
    final byte VALUE_RES = (byte) VALUE;

    push(VALUE);

    processor.process(CLASS_PROCESSOR_MOCK, new I2B(), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE_RES, (short) pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_Positive() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(I2B.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = 57;
    final byte VALUE_RES = (byte) VALUE;

    push(VALUE);

    processor.process(CLASS_PROCESSOR_MOCK, new I2B(), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE_RES, (short) pop());
    assertEquals(INIT_SP, SP);
  }

}
