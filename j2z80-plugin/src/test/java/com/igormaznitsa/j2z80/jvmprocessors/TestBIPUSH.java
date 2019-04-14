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

import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestBIPUSH extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution_Positive() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(BIPUSH.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = 123;

    processor.process(CLASS_PROCESSOR_MOCK, new BIPUSH((byte) VALUE), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE, pop());
    assertEquals(INIT_SP, SP);
  }

  @Test
  public void testExecution_Negative() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(BIPUSH.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = -123;

    processor.process(CLASS_PROCESSOR_MOCK, new BIPUSH((byte) VALUE), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE, (short) pop());
    assertEquals(INIT_SP, SP);
  }

}
