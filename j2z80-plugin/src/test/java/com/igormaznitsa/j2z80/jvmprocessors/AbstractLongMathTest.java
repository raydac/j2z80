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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.igormaznitsa.j2z80.utils.Utils;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

public abstract class AbstractLongMathTest extends AbstractJvmCommandProcessorTest {

  @Override
  public String getAsmPostfix() {
    try {
      final String longs =
          Utils.readTextResource(AbstractJvmCommandProcessor.class, "LONG_ARITHMETIC_MANAGER.a80");
      final String floats =
          Utils.readTextResource(AbstractJvmCommandProcessor.class, "FLOAT_ARITHMETIC_MANAGER.a80");
      final String athrow =
          Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
      return "JP " + END_LABEL + "\n" + longs + "\n" + floats + "\n" + athrow;
    } catch (IOException ex) {
      fail("Can't load long math block");
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

  protected void pushLong(final int value) {
    this.push((value >>> 16) & 0xFFFF);
    this.push(value & 0xFFFF);
  }

  protected int popLong() {
    final int low = this.pop() & 0xFFFF;
    final int high = this.pop() & 0xFFFF;
    return (high << 16) | low;
  }

  protected int runBinary(final Instruction instruction, final int left, final int right)
      throws Exception {
    this.beforeTest();
    this.pushLong(left);
    this.pushLong(right);
    this.assertLinearExecutionToEnd(this.translate(instruction));
    final int result = this.popLong();
    this.assertStackEmpty();
    return result;
  }

  protected int runUnary(final Instruction instruction, final int value) throws Exception {
    this.beforeTest();
    this.pushLong(value);
    this.assertLinearExecutionToEnd(this.translate(instruction));
    final int result = this.popLong();
    this.assertStackEmpty();
    return result;
  }
}
