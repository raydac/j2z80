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
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestIADD extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(IADD.class);
    final StringWriter writer = new StringWriter();

    final int VAL1 = 1345;
    final int VAL2 = -45;

    push(VAL1);
    push(VAL2);

    processor.process(CLASS_PROCESSOR_MOCK, new IADD(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    assertLinearExecutionToEnd(writer.toString());

    assertEquals(VAL1 + VAL2, (short) pop());
    assertEquals(INIT_SP, SP);
  }
}
