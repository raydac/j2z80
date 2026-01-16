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
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestALOAD extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution_index0() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ALOAD.class);
    final StringWriter writer = new StringWriter();

    final int VAR_INDEX = 0;
    final int IX_BASE = 0x7000;
    final int VALUE = 0xCAFE;

    pokew(IX_BASE - (VAR_INDEX << 1), VALUE);
    IX = IX_BASE;

    processor.process(CLASS_PROCESSOR_MOCK, new ALOAD(VAR_INDEX), mock(InstructionHandle.class),
        this.getClass().getClassLoader(),
        writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_index55() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ALOAD.class);
    final StringWriter writer = new StringWriter();

    final int VAR_INDEX = 55;
    final int IX_BASE = 0x7000;
    final int VALUE = 0xCAFE;

    pokew(IX_BASE - (VAR_INDEX << 1), VALUE);
    IX = IX_BASE;

    processor.process(CLASS_PROCESSOR_MOCK, new ALOAD(VAR_INDEX), mock(InstructionHandle.class),
        this.getClass().getClassLoader(),
        writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE, pop());
    assertEquals(INIT_SP, SP);
  }

}
