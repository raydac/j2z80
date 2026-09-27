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

import static com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager.PENDING_EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestATHROW extends AbstractJvmCommandProcessorTest {

  @Test
  public void uncaughtAthrowResetsWhenMethodDoesNotDeclareThrows() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ATHROW.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new ATHROW(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);

    final String asm = writer.toString();
    assertTrue(asm.contains("POP BC"));
    assertTrue(asm.contains("JP 0"));
  }

  @Test
  public void pendingExceptionCellIsDefinedInManager() {
    assertEquals("___PENDING_EXCEPTION", PENDING_EXCEPTION);
  }
}
