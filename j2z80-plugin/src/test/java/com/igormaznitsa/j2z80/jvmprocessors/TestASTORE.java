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

import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestASTORE extends AbstractJvmCommandProcessorTest {

  static final int IX_BASE = 0x7000;
  static final int VALUE = 0xCAFE;
  final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ASTORE.class);

  @Test
  public void testExecution_index0() throws IOException {
    final StringWriter writer = new StringWriter();
    final int VAR_INDEX = 0;
    pokew(IX_BASE - (VAR_INDEX << 1), VALUE);
    IX = IX_BASE;
    push(VALUE);
    processor.process(CLASS_PROCESSOR_MOCK, new ASTORE(VAR_INDEX), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(VALUE, peekw(IX_BASE - (VAR_INDEX << 1)));
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_index30() throws IOException {
    final StringWriter writer = new StringWriter();
    final int VAR_INDEX = 30;
    pokew(IX_BASE - (VAR_INDEX << 1), VALUE);
    IX = IX_BASE;
    push(VALUE);
    processor.process(CLASS_PROCESSOR_MOCK, new ASTORE(VAR_INDEX), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(VALUE, peekw(IX_BASE - (VAR_INDEX << 1)));
    assertEquals(INIT_SP, SP);
  }
}
