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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.igormaznitsa.j2z80.utils.Utils;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

public abstract class AbstractFloatMathTest extends AbstractJvmCommandProcessorTest {

  @Override
  public String getAsmPostfix() {
    try {
      final String block =
          Utils.readTextResource(AbstractJvmCommandProcessor.class, "FLOAT_ARITHMETIC_MANAGER.a80");
      return "JP " + END_LABEL + "\n" + block;
    } catch (IOException ex) {
      fail("Can't load float math block");
      return null;
    }
  }

  protected String translate(final Instruction instruction) throws Exception {
    final AbstractJvmCommandProcessor processor =
        AbstractJvmCommandProcessor.findProcessor(instruction.getClass());
    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, instruction, mock(InstructionHandle.class),
        this.getClass().getClassLoader(), writer);
    return writer.toString();
  }

  protected int runUnary(final Instruction instruction, final int value) throws Exception {
    this.beforeTest();
    this.push(value);
    this.assertLinearExecutionToEnd(this.translate(instruction));
    final int result = this.pop() & 0xFFFF;
    this.assertStackEmpty();
    return result;
  }

  protected int runBinary(final Instruction instruction, final int left, final int right)
      throws Exception {
    this.beforeTest();
    this.push(left);
    this.push(right);
    this.assertLinearExecutionToEnd(this.translate(instruction));
    final int result = this.pop() & 0xFFFF;
    this.assertStackEmpty();
    return result;
  }
}
