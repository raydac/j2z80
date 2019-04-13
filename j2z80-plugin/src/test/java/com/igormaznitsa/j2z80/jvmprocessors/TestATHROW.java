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

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestATHROW extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ATHROW.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = 0xCAFE;

    push(VALUE);

    processor.process(CLASS_PROCESSOR_MOCK, new ATHROW(), mock(InstructionHandle.class), writer);
    final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());

    assertEquals(asm.findLabelAddress(END_LABEL).intValue(), HL());
    assertEquals(asm.findLabelAddress("SSSS").intValue(), pop());
    assertEquals(VALUE, pop());

    assertStackEmpty();
  }

  @Override
  public String getAsmPrefix() {
    return "LD HL," + END_LABEL + "\n" + "LD (" + NeedsATHROWManager.ATHROW_PROCESSING_ADDRESS + "),HL\n SSSS:\n";
  }

  @Override
  public String getAsmPostfix() {
    try {
      final String str = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
      return "FOREVER: JR FOREVER\n" + str;
    } catch (IOException ex) {
      fail("IOException, can't read text resource");
      return null;
    }
  }

}
