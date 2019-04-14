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

import javassist.bytecode.AccessFlag;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestINVOKESTATIC extends AbstractInvokeTest {

  private static final String TEST_EXPRESSION_4_LABEL = "TEST_EXPRESSION_4";
  private static final String TEST_333_LABEL = "TEST_333_LABEL";
  private static final int TEST_LOCALS_NUMBER = 28;

  private final INVOKESTATIC INSTRUCTION_INSTANCE = new INVOKESTATIC(CONSTANT_MOCK_METHOD);

  private final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

  @Test(timeout = 3000L)
  public void testExecutionWithoutArgumentsAndVoidResultWithoutLocals() throws Exception {
    final Type RESULT_TYPE = Type.VOID;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, 0, ARGS_NULL, RESULT_TYPE);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint(null, writer);

    assertLinearExecutionToEnd(writer.toString());

    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertElementsOnStack(1);
  }

  @Test(timeout = 3000L)
  public void testExecutionWithoutArgumentsAndVoidResultWithLocals() throws Exception {
    final Type RESULT_TYPE = Type.VOID;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, TEST_LOCALS_NUMBER, ARGS_NULL, RESULT_TYPE);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint("NOLABEL", writer);

    assertLinearExecutionToEnd(writer.toString());

    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testExecutionWithoutArgumentsAndIntResultWithoutLocals() throws Exception {
    final Type RESULT_TYPE = Type.INT;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, 0, ARGS_NULL, RESULT_TYPE);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint(TEST_333_LABEL, writer);

    registerBreakPoint(TEST_333_LABEL);

    IX(INITIAL_IX);

    assertLinearExecutionToEnd(writer.toString());

    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertEquals(INITIAL_IX, IX);
    assertEquals(333, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testExecutionWithoutArgumentsAndIntResultWithLocals() throws Exception {
    final Type RESULT_TYPE = Type.INT;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, TEST_LOCALS_NUMBER, ARGS_NULL, RESULT_TYPE);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint(TEST_333_LABEL, writer);

    registerBreakPoint(TEST_333_LABEL);

    IX(INITIAL_IX);

    assertLinearExecutionToEnd(writer.toString());

    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertEquals(INITIAL_IX, IX);
    assertEquals(333, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testExecutionWithArgumentsAndIntResultWithoutLocals() throws Exception {
    final Type RESULT_TYPE = Type.INT;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, 0, ARGS_FOUR_INT, RESULT_TYPE);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, writer);

    final int arg1 = 0xCAFE;
    final int arg2 = 0xBABE;
    final int arg3 = 0xC0FF;
    final int arg4 = 0x1234;

    push(arg1);
    push(arg2);
    push(arg3);
    push(arg4);

    registerBreakPoint(TEST_EXPRESSION_4_LABEL);
    IX(INITIAL_IX);
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertEquals(INITIAL_IX, IX);
    assertEquals((short) testExpression(arg1, arg2, arg3, arg4), (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testExecutionWithArgumentsAndIntResultWithLocals() throws Exception {
    final Type RESULT_TYPE = Type.INT;

    mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.STATIC, TEST_LOCALS_NUMBER, ARGS_FOUR_INT, RESULT_TYPE);

    final StringWriter writer = new StringWriter();
    processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
    makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, writer);

    final int arg1 = 0xCAFE;
    final int arg2 = 0xBABE;
    final int arg3 = 0xC0FF;
    final int arg4 = 0x1234;

    push(arg1);
    push(arg2);
    push(arg3);
    push(arg4);

    registerBreakPoint(TEST_EXPRESSION_4_LABEL);
    IX(INITIAL_IX);
    assertLinearExecutionToEnd(writer.toString());
    assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
    assertEquals(INITIAL_IX, IX);
    assertEquals((short) testExpression(arg1, arg2, arg3, arg4), (short) pop());
    assertStackEmpty();
  }

  private int testExpression(final int a, final int b, final int c, final int d) {
    return a + b - c * d;
  }

  @Override
  public void processBreakPoint(final String label) {
    boolean breakpointmet = false;
    if (TEST_EXPRESSION_4_LABEL.equals(label)) {
      BC(testExpression(readLocalFrameVariable(0), readLocalFrameVariable(1), readLocalFrameVariable(2), readLocalFrameVariable(3)));
      breakpointmet = true;
    } else if (TEST_333_LABEL.equals(label)) {
      BC(333);
      breakpointmet = true;
    }

    if (breakpointmet && mockupOfInvokedMethod.getMaxLocals() != 0) {
      // clear all locals
      final int localsNumber = mockupOfInvokedMethod.getMaxLocals();
      for (int i = 0; i < localsNumber; i++) {
        writeLocalFrameVariable(i, 0xCACA);
      }
    }
  }

}
