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

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.RET;
import org.junit.Test;

public class TestRET extends AbstractJvmCommandProcessorTest {

  @Test//(timeout=3000L)
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(RET.class);
    final StringWriter writer = new StringWriter();

    final int IX_ADDRESS = 0x8000;
    final int INDEX = 23;

    processor.process(CLASS_PROCESSOR_MOCK, new RET(INDEX), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);

    IX = IX_ADDRESS;

    assertLinearExecutionToEnd("ld hl," + END_LABEL + "\n ld (" + (IX_ADDRESS - (INDEX << 1)) + "),hl \n " + writer.toString() + "\n LOOP: JR LOOP\n");
    assertStackEmpty();
  }

}
