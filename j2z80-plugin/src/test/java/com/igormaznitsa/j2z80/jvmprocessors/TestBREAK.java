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
package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.api.additional.NeedsBREAKPOINTManager;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import org.apache.bcel.generic.BREAKPOINT;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestBREAK extends AbstractJvmCommandProcessorTest {
  private static final int VALUE = 0xCAFE;
  private static final int TEST_ADDRESS = 258;
  private static final String BREAK_POINT_START_ADDRESS = "BP_START_LABEL";

  @Test(timeout = 3000L)
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(BREAKPOINT.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new BREAKPOINT(), mock(InstructionHandle.class), writer);
    final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());

    assertEquals(asm.findLabelAddress(BREAK_POINT_START_ADDRESS).intValue(), peekw(TEST_ADDRESS));
    assertStackEmpty();
  }

  @Override
  public String getAsmPrefix() {
    return "LD HL,BP_PROCESSOR\nLD (" + NeedsBREAKPOINTManager.BREAKPOINT_PROCESSING_SUB_ADDRESS + "),HL\n " + BREAK_POINT_START_ADDRESS + ":\n";
  }

  @Override
  public String getAsmPostfix() {
    try {
      final String str = Utils.readTextResource(AbstractJvmCommandProcessor.class, "BREAKPOINT_MANAGER.a80");
      return "JR " + END_LABEL + "\n BP_PROCESSOR: LD (" + TEST_ADDRESS + "),HL\n RET\n" + str;
    } catch (IOException ex) {
      fail("IOException, can't read text resource");
      return null;
    }
  }

}
