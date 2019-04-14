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
