/*
 * Copyright 2012-2026 Igor Maznitsa.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LALOAD;
import org.apache.bcel.generic.LASTORE;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LRETURN;
import org.apache.bcel.generic.LSTORE;
import org.junit.Test;

public class TestLongSlots extends AbstractJvmCommandProcessorTest {

  @Test
  public void loadsLongConstant() throws Exception {
    final StringWriter writer = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LCONST.class)
        .process(CLASS_PROCESSOR_MOCK, new LCONST(1L), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), writer);
    this.assertLinearExecutionToEnd(writer.toString());
    assertEquals(1, this.pop());
    assertEquals(0, this.pop());
    this.assertStackEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsLongConstantOutside32Bits() throws Exception {
    final StringWriter writer = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LDC2_W.class)
        .process(CLASS_PROCESSOR_MOCK, new LDC2_W(CONSTANT_LONG), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), writer);
  }

  @Test
  public void loadsFittingLongConstant() throws Exception {
    when(CP_GEN_MOCK.getConstant(CONSTANT_LONG)).thenReturn(new ConstantLong(0x12345678L));
    final StringWriter writer = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LDC2_W.class)
        .process(CLASS_PROCESSOR_MOCK, new LDC2_W(CONSTANT_LONG), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), writer);
    this.assertLinearExecutionToEnd(writer.toString());
    assertEquals(0x5678, this.pop());
    assertEquals(0x1234, this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void storesAndLoadsLocal() throws Exception {
    final int index = 2;
    IX = 0x7000;
    this.push(0x1234);
    this.push(0x5678);
    final StringWriter store = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LSTORE.class)
        .process(CLASS_PROCESSOR_MOCK, new LSTORE(index), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), store);
    this.assertLinearExecutionToEnd(store.toString());
    assertEquals(0x5678, this.readLocalFrameVariable(index));
    assertEquals(0x1234, this.readLocalFrameVariable(index + 1));

    this.beforeTest();
    IX = 0x7000;
    this.writeLocalFrameVariable(index, 0x5678);
    this.writeLocalFrameVariable(index + 1, 0x1234);
    final StringWriter load = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LLOAD.class)
        .process(CLASS_PROCESSOR_MOCK, new LLOAD(index), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), load);
    this.assertLinearExecutionToEnd(load.toString());
    assertEquals(0x5678, this.pop());
    assertEquals(0x1234, this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void storesAndLoadsArrayElement() throws Exception {
    final int array = 0x8000;
    final int index = 2;
    this.push(array);
    this.push(index);
    this.push(0x1111);
    this.push(0x2222);
    final StringWriter store = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LASTORE.class)
        .process(CLASS_PROCESSOR_MOCK, new LASTORE(), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), store);
    this.assertLinearExecutionToEnd(store.toString());
    assertEquals(0x2222, this.peekw(array + (index << 2)));
    assertEquals(0x1111, this.peekw(array + (index << 2) + 2));

    this.beforeTest();
    this.pokew(array + (index << 2), 0x2222);
    this.pokew(array + (index << 2) + 2, 0x1111);
    this.push(array);
    this.push(index);
    final StringWriter load = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LALOAD.class)
        .process(CLASS_PROCESSOR_MOCK, new LALOAD(), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), load);
    this.assertLinearExecutionToEnd(load.toString());
    assertEquals(0x2222, this.pop());
    assertEquals(0x1111, this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void returnsLongInBcAndDe() throws Exception {
    final StringWriter writer = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(LRETURN.class)
        .process(CLASS_PROCESSOR_MOCK, new LRETURN(), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), writer);
    this.assertLinearExecutionToEnd(
        "ld hl," + END_LABEL + "\n push hl\n ld hl,#1234\n push hl\n ld hl,#5678\n push hl\n" +
            writer);
    assertEquals(0x5678, this.BC());
    assertEquals(0x1234, this.DE());
    this.assertStackEmpty();
  }
}
