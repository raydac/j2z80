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
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;

public class TestATHROW extends AbstractJvmCommandProcessorTest {

  @Test
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ATHROW.class);
    final StringWriter writer = new StringWriter();
    final int VALUE = 0xCAFE;

    push(VALUE);

    processor.process(CLASS_PROCESSOR_MOCK, new ATHROW(), mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());

    assertEquals(asm.findLabelAddress(END_LABEL).intValue(), HL());
    assertEquals(asm.findLabelAddress("SSSS").intValue(), pop());
    assertEquals(VALUE, pop());

    assertStackEmpty();
  }

  @Override
  public String getAsmPrefix() {
    return "LD HL," + END_LABEL + "\n" + "LD (" + NeedsATHROWManager.ATHROW_PROCESSING_ADDRESS + "),HL\n SSSS:\n";
  }

  @Override
  public String getAsmPostfix() {
    try {
      final String str = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
      return "FOREVER: JR FOREVER\n" + str;
    } catch (IOException ex) {
      fail("IOException, can't read text resource");
      return null;
    }
  }

}
