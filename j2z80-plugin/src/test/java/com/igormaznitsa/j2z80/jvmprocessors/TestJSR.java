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

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.JSR;
import org.apache.bcel.generic.NOP;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class TestJSR extends AbstractJvmCommandProcessorTest {

  private final NOP nop = new NOP();
  private InstructionHandle targetHandle;

  @Test(timeout = 3000L)
  public void testJSR() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(JSR.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = START_ADDRESS + 3;

    final JSR instruction = new JSR(null);

    final BranchHandle branchHandle = Whitebox.invokeMethod(BranchHandle.class, "getBranchHandle", instruction);

    targetHandle = Whitebox.invokeMethod(BranchHandle.class, "getInstructionHandle", nop);

    branchHandle.setTarget(targetHandle);

    processor.process(CLASS_PROCESSOR_MOCK, instruction, branchHandle, writer);

    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE, pop());
    assertEquals(INIT_SP, SP);

  }

  @Override
  public String getAsmPostfix() {
    final String label = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, targetHandle.getPosition());

    return "FLOOP: JR FLOOP\n" + label + ":\n";
  }
}
