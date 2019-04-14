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
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.TABLESWITCH;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import java.io.StringWriter;

public abstract class AbstractJVMSelectTest extends AbstractJvmCommandProcessorTest {

  public static final int DEFAULT_POSITION = 0xFFFF;

  public void executeSelectInstruction(final Class<? extends Select> instruction, final int checkValue, final int[] allowedIndexes) throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction);

    int checkedIndex = -1;

    for (int i = 0; i < allowedIndexes.length; i++) {
      if (allowedIndexes[i] == checkValue) {
        checkedIndex = i;
      }
    }

    final InstructionHandle[] targets = mockTargets(allowedIndexes);
    final InstructionHandle defaultTarget = makeTargetMock(0xFFFF);

    Select instance = null;
    if (instruction == TABLESWITCH.class) {
      instance = new TABLESWITCH(allowedIndexes, targets, defaultTarget);
    } else if (instruction == LOOKUPSWITCH.class) {
      instance = new LOOKUPSWITCH(allowedIndexes, targets, defaultTarget);
    } else {
      throw new IllegalArgumentException("Unsupported select instruction");
    }

    final StringWriter out = new StringWriter();

    final BranchHandle mockInstructionHandle = Whitebox.invokeMethod(BranchHandle.class, "getBranchHandle", instance);

    processor.process(CLASS_PROCESSOR_MOCK, instance, mockInstructionHandle, out);

    for (int i = 0; i < targets.length; i++) {
      final String label = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, targets[i].getPosition());
      final String asm = makeAsmStubForIndex(targets[i], allowedIndexes[i], label);
      out.append(asm);
    }

    out.append(makeAsmStubForIndex(defaultTarget, DEFAULT_POSITION, LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, defaultTarget.getPosition())));

    push(checkValue);

    assertLinearExecutionToEnd(out.toString());
  }

  private String makeAsmStubForIndex(final InstructionHandle handle, final int data, final String defaultLabel) {
    final StringBuilder buffer = new StringBuilder();

    final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, handle.getPosition());
    buffer.append(jumpLabel + ":\n");
    buffer.append("LD HL,").append(data).append("\n");
    buffer.append("PUSH HL\n");
    buffer.append("JP ").append(END_LABEL).append("\n");

    return buffer.toString();
  }

  private InstructionHandle makeTargetMock(final int position) throws Exception {

    final InstructionHandle handle = Mockito.spy((InstructionHandle) Whitebox.invokeMethod(InstructionHandle.class, "getInstructionHandle", new LDC(1)));
    Mockito.doReturn(position).when(handle).getPosition();
    Mockito.doReturn(Boolean.TRUE).when(handle).hasTargeters();
    return handle;
  }

  private InstructionHandle[] mockTargets(final int[] values) throws Exception {
    final InstructionHandle[] result = new InstructionHandle[values.length];

    int index = 0;
    int position = 100;
    for (final int value : values) {
      final InstructionHandle handle = makeTargetMock(position);
      position += 10;
      result[index++] = handle;
    }

    return result;
  }
}
