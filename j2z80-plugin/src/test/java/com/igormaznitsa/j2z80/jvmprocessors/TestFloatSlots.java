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

import com.igormaznitsa.j2z80.translator.utils.HalfFloat;
import java.io.StringWriter;
import org.apache.bcel.generic.FALOAD;
import org.apache.bcel.generic.FASTORE;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.FRETURN;
import org.apache.bcel.generic.FSTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestFloatSlots extends AbstractJvmCommandProcessorTest {

  @Test
  public void loadsFloatConstant() throws Exception {
    final AbstractJvmCommandProcessor processor =
        AbstractJvmCommandProcessor.findProcessor(FCONST.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new FCONST(2f), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    this.assertLinearExecutionToEnd(writer.toString());
    assertEquals(HalfFloat.toBits(2f), this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void storesAndLoadsLocal() throws Exception {
    final int index = 4;
    final int bits = HalfFloat.toBits(1.5f);
    IX = 0x7000;
    this.push(bits);
    final StringWriter store = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(FSTORE.class)
        .process(CLASS_PROCESSOR_MOCK, new FSTORE(index), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), store);
    this.assertLinearExecutionToEnd(store.toString());
    assertEquals(bits, this.readLocalFrameVariable(index));

    this.beforeTest();
    IX = 0x7000;
    this.writeLocalFrameVariable(index, bits);
    final StringWriter load = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(FLOAD.class)
        .process(CLASS_PROCESSOR_MOCK, new FLOAD(index), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), load);
    this.assertLinearExecutionToEnd(load.toString());
    assertEquals(bits, this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void storesAndLoadsArrayElement() throws Exception {
    final int array = 0x8000;
    final int index = 3;
    final int bits = HalfFloat.toBits(-4f);
    this.push(array);
    this.push(index);
    this.push(bits);
    final StringWriter store = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(FASTORE.class)
        .process(CLASS_PROCESSOR_MOCK, new FASTORE(), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), store);
    this.assertLinearExecutionToEnd(store.toString());
    assertEquals(bits, this.peekw(array + (index << 1)));

    this.beforeTest();
    this.pokew(array + (index << 1), bits);
    this.push(array);
    this.push(index);
    final StringWriter load = new StringWriter();
    AbstractJvmCommandProcessor.findProcessor(FALOAD.class)
        .process(CLASS_PROCESSOR_MOCK, new FALOAD(), mock(InstructionHandle.class),
            this.getClass().getClassLoader(), load);
    this.assertLinearExecutionToEnd(load.toString());
    assertEquals(bits, this.pop());
    this.assertStackEmpty();
  }

  @Test
  public void returnsFloatInBc() throws Exception {
    final AbstractJvmCommandProcessor processor =
        AbstractJvmCommandProcessor.findProcessor(FRETURN.class);
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, new FRETURN(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    this.assertLinearExecutionToEnd(
        "ld hl," + END_LABEL + "\n push hl\n ld hl,#3C00\n push hl\n" + writer);
    assertEquals(0x3C00, this.BC());
    this.assertStackEmpty();
  }
}
