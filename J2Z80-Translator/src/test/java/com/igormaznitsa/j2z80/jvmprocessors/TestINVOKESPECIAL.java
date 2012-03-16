/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 * 
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.igormaznitsa.j2z80.jvmprocessors;

import java.io.StringWriter;
import javassist.bytecode.AccessFlag;
import org.apache.bcel.generic.*;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TestINVOKESPECIAL extends AbstractInvokeTest {

    private static final String TEST_EXPRESSION_4_LABEL = "TEST_EXPRESSION_4";
    private static final String TEST_333_LABEL = "TEST_333_LABEL";
    private static final int TEST_LOCALS_NUMBER = 28;

    private static final int FAKE_OBJECT_ADDRESS = 0xAFA0;

    private final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INVOKESPECIAL.class);
    private final INVOKESPECIAL INSTRUCTION_INSTANCE = new INVOKESPECIAL(CONSTANT_MOCK_METHOD);
    
    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndVoidResultWithoutLocals() throws Exception {
        final Type RESULT_TYPE = Type.VOID;
        
        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, 0, ARGS_NULL, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(null,writer);

        IX(INITIAL_IX);
        
        push(FAKE_OBJECT_ADDRESS);
        assertLinearExecutionToEnd(writer.toString());

        assertEquals("Check that the metod has been called",FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertEquals("Check that we have the object ref as the zero local",readLocalFrameVariable(0),FAKE_OBJECT_ADDRESS);
        pop(); //skip return address on the stack
        
        // simulation of invoke postfix sub
        assertEquals("Saved IX",INITIAL_IX,pop());        
        SP = IX;
        SP += 2;
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndVoidResultWithLocals() throws Exception {
        final Type RESULT_TYPE = Type.VOID;

        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, TEST_LOCALS_NUMBER, ARGS_NULL, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint("NOLABEL", writer);

        push(FAKE_OBJECT_ADDRESS);
        assertLinearExecutionToEnd(writer.toString());

        assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndIntResultWithoutLocals() throws Exception {
        final Type RESULT_TYPE = Type.INT;

        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, 0, ARGS_NULL, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_333_LABEL, writer);

        registerBreakPoint(TEST_333_LABEL);

        IX(INITIAL_IX);
        push(FAKE_OBJECT_ADDRESS);
        assertLinearExecutionToEnd(writer.toString());

        assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertEquals(INITIAL_IX, IX);
        assertEquals(333, pop());
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndIntResultWithLocals() throws Exception {
        final Type RESULT_TYPE = Type.INT;
        final String TEST_METHOD = "someNonStaticMethod";

        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, TEST_LOCALS_NUMBER, ARGS_NULL, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_333_LABEL, writer);

        registerBreakPoint(TEST_333_LABEL);

        IX(INITIAL_IX);
        push(FAKE_OBJECT_ADDRESS);
        assertLinearExecutionToEnd(writer.toString());

        assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertEquals(INITIAL_IX, IX);
        assertEquals(333, pop());
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithArgumentsAndIntResultWithoutLocals() throws Exception {
        final Type RESULT_TYPE = Type.INT;

        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, 0, ARGS_FOUR_INT, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, writer);

        final int arg1 = 0xCAFE;
        final int arg2 = 0xBABE;
        final int arg3 = 0xC0FF;
        final int arg4 = 0x1234;

        push(FAKE_OBJECT_ADDRESS);
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

        mockupOfInvokedMethod = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD,AccessFlag.PUBLIC, TEST_LOCALS_NUMBER, ARGS_FOUR_INT, RESULT_TYPE);

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, INSTRUCTION_INSTANCE, mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, writer);

        final int arg1 = 0xCAFE;
        final int arg2 = 0xBABE;
        final int arg3 = 0xC0FF;
        final int arg4 = 0x1234;

        push(FAKE_OBJECT_ADDRESS);
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
        assertEquals(FAKE_OBJECT_ADDRESS,readLocalFrameVariable(0));
        boolean breakpointmet = false;
        if (TEST_EXPRESSION_4_LABEL.equals(label)) {
            BC(testExpression(readLocalFrameVariable(1), readLocalFrameVariable(2), readLocalFrameVariable(3), readLocalFrameVariable(4)));
            breakpointmet = true;
        } else if (TEST_333_LABEL.equals(label)) {
            BC(333);
            breakpointmet = true;
        }

        if (breakpointmet && mockupOfInvokedMethod.getMaxLocals() != 0) {
            // clear all locals
            final int localsNumber = mockupOfInvokedMethod.getMaxLocals();
            int addr = IX;
            for (int i = 0; i < (localsNumber+1); i++) {
                writeLocalFrameVariable(i, 0xCACA);
            }
        }
    }

}
