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

import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestIINC extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IINC.class);
    final StringWriter writer = new StringWriter();
    final int IX_ADDRESS = 0x8000;
    final int INDEX = 23;
    final int VALUE = 10823;
    final int INC_VALUE = 22;

    pokew(IX_ADDRESS - (INDEX << 1), VALUE);

    IX = IX_ADDRESS;
    processor.process(CLASS_PROCESSOR_MOCK, new IINC(INDEX, INC_VALUE), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE + INC_VALUE, (short) peekw(IX_ADDRESS - (INDEX << 1)));
    assertEquals(INIT_SP, SP);
  }
}
