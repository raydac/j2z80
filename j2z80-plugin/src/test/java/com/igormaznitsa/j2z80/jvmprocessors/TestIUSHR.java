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

import org.apache.bcel.generic.IUSHR;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestIUSHR extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution_zeroShift() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFE;
    final int SHIFT = 0x0;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VAL >>> SHIFT, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_shiftNegativeNumber() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFAE0;
    final int SHIFT = 0x8;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(0xFA, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_shiftPositiveNumber() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFE;
    final int SHIFT = 0x7;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VAL >>> SHIFT, pop());
    assertEquals(INIT_SP, SP);
  }
}
