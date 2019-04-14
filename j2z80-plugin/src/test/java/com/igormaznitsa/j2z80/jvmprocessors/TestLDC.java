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
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestLDC extends AbstractJvmCommandProcessorTest {

  @Test
  public void testInt() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_INT), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(ETALON_CONSTANT_INTEGER, (short) pop());
    assertStackEmpty();

    verify(CLASS_PROCESSOR_MOCK, never()).registerUsedConstantPoolItem(CONSTANT_INT);
  }

  @Test
  public void testUTF8() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();

    final String etalonLabel = LabelAndFrameUtils.makeLabelForConstantPoolItem(JCLASS_GEN_MOCK, CONSTANT_UTF8);
    when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_UTF8)).thenReturn(etalonLabel);

    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_UTF8), mock(InstructionHandle.class), writer);

    final String labelName = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);
    final int labelAddress = assertLinearExecutionToEnd(writer.toString()).findLabelAddress(labelName).intValue();
    assertEquals(labelAddress, pop());
    assertStackEmpty();

    verify(CLASS_PROCESSOR_MOCK).registerUsedConstantPoolItem(CONSTANT_UTF8);
  }

  @Test
  public void testString() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();

    final String labelName = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);

    when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_UTF8)).thenReturn(labelName);
    when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_STR)).thenReturn(labelName);

    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_STR), mock(InstructionHandle.class), writer);

    final int labelAddress = assertLinearExecutionToEnd(writer.toString()).findLabelAddress(labelName).intValue();
    assertEquals(labelAddress, pop());
    assertStackEmpty();

    verify(CLASS_PROCESSOR_MOCK, never()).registerUsedConstantPoolItem(CONSTANT_UTF8);
    verify(CLASS_PROCESSOR_MOCK).registerUsedConstantPoolItem(CONSTANT_STR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDouble() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_DOUBLE), mock(InstructionHandle.class), writer);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFloat() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_FLOAT), mock(InstructionHandle.class), writer);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLong() throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_LONG), mock(InstructionHandle.class), writer);
  }

  @Override
  public String getAsmPostfix() {
    final String labelUTF8 = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);
    final String labelStr = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_STR);
    final String labelInt = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_INT);
    return labelUTF8 + ": NOP\n" + labelInt + ": NOP\n" + labelStr + ": EQU " + labelUTF8;
  }
}
