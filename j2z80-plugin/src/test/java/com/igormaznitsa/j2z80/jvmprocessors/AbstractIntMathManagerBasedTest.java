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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.utils.Utils;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

public abstract class AbstractIntMathManagerBasedTest extends AbstractJvmCommandProcessorTest {

  private static final int EXCEPTION_FLAG_ADDRESS = 0x100;
  private static final int EXCEPTION_FLAG = 0xCA;

  public String prepareForTest(final Instruction instruction) throws Exception {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, instruction, mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    return writer.toString();
  }

  @Override
  public String getAsmPostfix() {
    try {
      final String arithblock = Utils.readTextResource(AbstractJvmCommandProcessor.class, "INT_ARITHMETIC_MANAGER.a80");
      final String athrowmanager = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
      return "JP " + END_LABEL + "\n" + arithblock + "\n" + athrowmanager;
    } catch (IOException ex) {
      fail("Can't load INT Math code block");
      return null;
    }
  }

  public void assertException(final String asm) {
    final String exceptionCode = "XOR A\n LD (" + EXCEPTION_FLAG_ADDRESS + "),A\n LD HL,ARITH_EXCEPTION\n LD  (" + NeedsATHROWManager.ATHROW_PROCESSING_ADDRESS + "),HL\n"
        + "JP ENDEX\n ARITH_EXCEPTION: POP HL\n LD A," + EXCEPTION_FLAG + "\n LD (" + EXCEPTION_FLAG_ADDRESS + "),A\n JP " + END_LABEL + "\nENDEX:\n";

    assertLinearExecutionToEnd(exceptionCode + asm);
    assertEquals("Exception block must be called", EXCEPTION_FLAG, peekb(EXCEPTION_FLAG_ADDRESS));
  }
}
