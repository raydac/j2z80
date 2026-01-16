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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.IUSHR;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestIUSHR extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution_zeroShift() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFE;
    final int SHIFT = 0x0;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VAL >>> SHIFT, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_shiftNegativeNumber() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFAE0;
    final int SHIFT = 0x8;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(0xFA, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_shiftPositiveNumber() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IUSHR.class);
    final StringWriter writer = new StringWriter();

    final int VAL = 0xFE;
    final int SHIFT = 0x7;

    push(VAL);
    push(SHIFT);

    processor.process(CLASS_PROCESSOR_MOCK, new IUSHR(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VAL >>> SHIFT, pop());
    assertEquals(INIT_SP, SP);
  }
}
