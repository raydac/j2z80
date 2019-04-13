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

import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestIINC extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IINC.class);
    final StringWriter writer = new StringWriter();
    final int IX_ADDRESS = 0x8000;
    final int INDEX = 23;
    final int VALUE = 10823;
    final int INC_VALUE = 22;

    pokew(IX_ADDRESS - (INDEX << 1), VALUE);

    IX = IX_ADDRESS;
    processor.process(CLASS_PROCESSOR_MOCK, new IINC(INDEX, INC_VALUE), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE + INC_VALUE, (short) peekw(IX_ADDRESS - (INDEX << 1)));
    assertEquals(INIT_SP, SP);
  }
}
