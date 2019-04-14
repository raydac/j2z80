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
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.powermock.reflect.Whitebox;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractJVMBranchTest extends AbstractJvmCommandProcessorTest {

  private String prepareTest(final Class<? extends BranchInstruction> instruction, final StringWriter writer) throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction);

    final int TARGET_INSTRUCTION_POS = 123;
    final InstructionHandle mockTarget = mock(InstructionHandle.class);
    when(mockTarget.getPosition()).thenReturn(Integer.valueOf(TARGET_INSTRUCTION_POS));
    final BranchInstruction instructionInstance = instruction.getConstructor(InstructionHandle.class).newInstance(mockTarget);
    final BranchHandle branchHandle = Whitebox.invokeMethod(BranchHandle.class, "getBranchHandle", instructionInstance);
    final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, branchHandle.getTarget().getPosition());

    processor.process(CLASS_PROCESSOR_MOCK, instructionInstance, branchHandle, writer);
    return jumpLabel;
  }

  public void assertTrueCondition(final Class<? extends BranchInstruction> instruction) throws Exception {
    final StringWriter writer = new StringWriter();
    final String jumpLabel = prepareTest(instruction, writer);

    writer.append("!LOOP: JP !LOOP\n");
    writer.append(jumpLabel + ": \n");
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(INIT_SP, SP);
  }

  public void assertFalseCondition(final Class<? extends BranchInstruction> instruction) throws Exception {
    final StringWriter writer = new StringWriter();
    final String jumpLabel = prepareTest(instruction, writer);

    writer.append("JR !!!FALSEEND\n");
    writer.append(jumpLabel + ": JP " + jumpLabel + "\n");
    writer.append("!!!FALSEEND: \n");

    assertLinearExecutionToEnd(writer.toString());
    assertEquals(INIT_SP, SP);
  }
}
