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
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestDUP_X2 extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(DUP_X2.class);
    final StringWriter writer = new StringWriter();
    final int VALUE1 = 0xAA01;
    final int VALUE2 = 0xCA02;
    final int VALUE3 = 0x1234;

    push(VALUE3);
    push(VALUE2);
    push(VALUE1);

    processor.process(CLASS_PROCESSOR_MOCK, new DUP_X2(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VALUE1, pop());
    assertEquals(VALUE2, pop());
    assertEquals(VALUE3, pop());
    assertEquals(VALUE1, pop());
    assertStackEmpty();
  }

}
