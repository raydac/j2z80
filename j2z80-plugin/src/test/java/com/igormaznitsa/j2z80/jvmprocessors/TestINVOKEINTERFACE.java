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

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.j2z80.utils.MutableObjectContainer;
import com.igormaznitsa.j2z80.api.additional.*;
import com.igormaznitsa.j2z80.ids.*;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.*;
import java.util.Locale;
import javassist.bytecode.AccessFlag;
import org.apache.bcel.generic.*;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TestINVOKEINTERFACE extends AbstractInvokeTest implements NeedsINVOKEINTERFACEManager, NeedsATHROWManager {

    private static final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INVOKEINTERFACE.class);
    private static final String MOCK_INTERFACE_NAME = "com.mockinterface";
    private static final String MOCK_INTERFACE_METHOD_NAME = "someinterfaceMethod";
    private static final String TEST_METHOD = "someNonStaticMethod";
    private static final int MOCK_INTERFACE_METHOD_INDEX = 2000;
    private static final int MOCK_INTERFACE_METHOD_ID = 0xCFA7;
    protected static String invokeInterfaceManager;
    protected static String athrowManager;
    protected String invokeinterfaceTable = "DEFB 0";
    private static final int EXCEPTION_FLAG = 0xACCA;
    protected String asmPrefix = "";
    private final MutableObjectContainer<MethodID> MOCK_METHOD_ID = new MutableObjectContainer<MethodID>();
    private final MutableObjectContainer<ClassID> MOCK_CLASS_ID = new MutableObjectContainer<ClassID>();
    private static final String EXCEPTION_PROCESSING_BLOCK = "LD HL,THR_LABEL\n"
            + "LD (" + ATHROW_PROCESSING_ADDRESS + "),HL\n"
            + "JP ENDTHRBLOCK\n"
            + "THR_LABEL: LD BC," + EXCEPTION_FLAG + "\n"
            + "PUSH BC\n"
            + "JP " + END_LABEL + "\n"
            + "ENDTHRBLOCK:\n";

    static {
        try {
            invokeInterfaceManager = Utils.readTextResource(AbstractJvmCommandProcessor.class, "INVOKEINTERFACE_MANAGER.a80");
        } catch (IOException ex) {
            throw new Error("Can't read invokeinterface manager assembler text");
        }
        try {
            athrowManager = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
        } catch (IOException ex) {
            throw new Error("Can't read athrow manager assembler text");
        }
    }
    private static final String TEST_EXPRESSION_4_LABEL = "TEST_EXPRESSION_4";
    private static final String TEST_333_LABEL = "TEST_333_LABEL";
    private static final int TEST_LOCALS_NUMBER = 28;
    private static final int FAKE_OBJECT_ADDRESS = 0xAFA0;
    private static final int FAKE_OBJECT_CLASS_ID = 0x1122;

    private void prepareFakeObjectInMemory() {
        pokew(FAKE_OBJECT_ADDRESS - 2, FAKE_OBJECT_CLASS_ID);
    }

    protected MethodID prepareInterfaceMockInTranslator(final Type result, final Type[] args, final int maxlocals) {
        final MethodID interfaceMethodID = new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, result, args);
        final ClassID mockClassID = new ClassID(MOCK_CLASS_NAME);

        registerMockMethod(MOCK_INTERFACE_METHOD_INDEX, MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, AccessFlag.INTERFACE, maxlocals, args, result);

        when(TRANSLATOR_MOCK.registerInterfaceMethodForINVOKEINTERFACE(eq(interfaceMethodID))).thenReturn(Integer.valueOf(MOCK_INTERFACE_METHOD_ID));

        when(CLASSCONTEXT_MOCK.findClassUID(eq(mockClassID))).thenReturn(Integer.valueOf(FAKE_OBJECT_CLASS_ID));
        when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(mockClassID))).thenReturn(Integer.valueOf(FAKE_OBJECT_CLASS_ID));

        return interfaceMethodID;
    }

     protected String generateInvokeInterfaceTable(final String interfaceMethodId, final long packedClassIDFrameSize, final String methodAddress) {
        final StringBuilder result = new StringBuilder();
        final int RECORDS = 8;

        result.append("DEFB ").append(RECORDS + 1).append('\n');

        for (int i = 0; i < RECORDS; i++) {
            result.append("DEFW ").append(i).append('\n');
            final int SUCCESSORS = 4;
            result.append("DEFB ").append(SUCCESSORS).append('\n');

            for (int l = 0; l < SUCCESSORS; l++) {
                result.append("DEFW ").append(i).append(',').append(l).append(',').append(12).append('\n');
            }
        }
        // save with the class in successors
        result.append("DEFW ").append(interfaceMethodId).append('\n');
        final int SUCCESSORS = 6;
        result.append("DEFB ").append(SUCCESSORS).append('\n');
        int i = 11;
        for (int l = 0; l < SUCCESSORS - 1; l++) {
            result.append("DEFW ").append(i++).append(',').append(l).append(',').append(12).append('\n');
        }
        result.append("DEFW #").append(Long.toHexString(packedClassIDFrameSize >>> 32).toUpperCase(Locale.ENGLISH)).append(',').append(methodAddress).append(',').append(packedClassIDFrameSize & 0xFFFFL).append('\n');

        return result.toString();
    }

    protected MethodGen makeSuccessorMethod(final MutableObjectContainer<MethodID> methodLabel, final MutableObjectContainer<ClassID> classLabel, final int maxLocals, final Type result, final Type[] args) {
        final MethodGen mockup = registerMockMethod(CONSTANT_MOCK_METHOD, TEST_INVOKED_CLASS, TEST_INVOKED_METHOD, AccessFlag.PUBLIC, maxLocals, args, result);
        methodLabel.set(new MethodID(MOCK_CLASS_NAME, TEST_METHOD, result, args));
        classLabel.set(new ClassID(MOCK_CLASS_NAME));
        prepareInterfaceMockInTranslator(result, args, maxLocals);
        return mockup;
    }

    protected long registerInterfaceMockMethod(final int maxLocals, final Type result, final Type[] types) {
        final Integer classId = TRANSLATOR_MOCK.getClassContext().findClassUID(MOCK_CLASS_ID.get());
        final int neededFrameSize = LabelAndFrameUtils.calculateFrameSizeForMethod(types.length,maxLocals,false);
        registerMockMethod(CONSTANT_MOCK_METHOD, MOCK_CLASS_NAME, TEST_METHOD, AccessFlag.PUBLIC, 0, types, result);
        prepareFakeObjectInMemory();
        return (((long) classId & 0xFFFFL) << 32) | ((long) neededFrameSize & 0xFFFFL);
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndVoidResultWithoutLocals() throws Exception {
        final Type RESULT_TYPE = Type.VOID;
        final int LOCALS = 0;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, ARGS_NULL));
        
        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, ARGS_NULL);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, ARGS_NULL);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(null, MOCK_CLASS_NAME, TEST_METHOD, ARGS_NULL, RESULT_TYPE, writer);

        IX(INITIAL_IX);
        push(FAKE_OBJECT_ADDRESS);
        final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());

        assertEquals("Check that the metod has been called", FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertEquals("Check that we have the object ref as the zero local", FAKE_OBJECT_ADDRESS, readLocalFrameVariable(0));
        pop(); //skip return address on the stack

        // simulation of invoke postfix sub
        assertEquals("Saved IX", INITIAL_IX, pop());
        SP = IX;
        SP += 2;
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndVoidResultWithLocals() throws Exception {
        final Type RESULT_TYPE = Type.VOID;
        final int LOCALS = TEST_LOCALS_NUMBER;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, ARGS_NULL));

        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, ARGS_NULL);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, ARGS_NULL);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint("NOLABEL", MOCK_CLASS_NAME, TEST_METHOD, ARGS_NULL, RESULT_TYPE, writer);

        push(FAKE_OBJECT_ADDRESS);

        assertLinearExecutionToEnd(writer.toString());

        assertEquals(FLAG_METHOD_CALLED, peekb(FLAG_ADDRESS));
        assertStackEmpty();
    }

    @Test(timeout = 3000L)
    public void testExecutionWithoutArgumentsAndIntResultWithoutLocals() throws Exception {
        final Type RESULT_TYPE = Type.INT;
        final int LOCALS = 0;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, ARGS_NULL));

        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, ARGS_NULL);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, ARGS_NULL);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_333_LABEL, MOCK_CLASS_NAME, TEST_METHOD, ARGS_NULL, RESULT_TYPE, writer);

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
        final int LOCALS = TEST_LOCALS_NUMBER;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, ARGS_NULL));

        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, ARGS_NULL);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, ARGS_NULL);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_333_LABEL, MOCK_CLASS_NAME, TEST_METHOD, ARGS_NULL, RESULT_TYPE, writer);

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
        final int LOCALS = 0;
        final Type[] args = ARGS_FOUR_INT;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, args));

        
        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, args);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, args);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, args.length + 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, MOCK_CLASS_NAME, TEST_METHOD, args, RESULT_TYPE, writer);

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
        final int LOCALS = TEST_LOCALS_NUMBER;
        final Type[] args = ARGS_FOUR_INT;
        
        final String LABEL_METHOD_ID = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, RESULT_TYPE, args));
        
        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, args);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, args);
        invokeinterfaceTable = generateInvokeInterfaceTable(LABEL_METHOD_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, args.length + 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, MOCK_CLASS_NAME, TEST_METHOD, args, RESULT_TYPE, writer);

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
    public void testExceptionBecauseSuccessorIsNotInTable() throws Exception {
        final Type RESULT_TYPE = Type.INT;
        final int LOCALS = 0;
        final Type[] args = ARGS_FOUR_INT;
        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, args);
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, args);
        invokeinterfaceTable = generateInvokeInterfaceTable("#" + Integer.toHexString(MOCK_INTERFACE_METHOD_ID).toUpperCase(Locale.ENGLISH), packedClassFrameInfo ^ 0x0000FFFFFFFF0000L, MOCK_METHOD_ID.get().getMethodLabel());

        // make asm prefix to notify us about exception
        asmPrefix = EXCEPTION_PROCESSING_BLOCK;

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, args.length + 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, MOCK_CLASS_NAME, TEST_METHOD, args, RESULT_TYPE, writer);

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

        assertEquals("Exception must be thrown", EXCEPTION_FLAG, pop());

        assertElementsOnStack(6);
    }

    @Test(timeout = 3000L)
    public void testExceptionBecauseInterfaceIsNotInTable() throws Exception {
        final Type RESULT_TYPE = Type.INT;
        final int LOCALS = 0;
        final Type[] args = ARGS_FOUR_INT;
        mockupOfInvokedMethod = makeSuccessorMethod(MOCK_METHOD_ID, MOCK_CLASS_ID, LOCALS, RESULT_TYPE, args);
        
        final String NOTINTABLE_INTERFACE_ID = "#1010";
        
        final long packedClassFrameInfo = registerInterfaceMockMethod(LOCALS, RESULT_TYPE, args);
        invokeinterfaceTable = generateInvokeInterfaceTable(NOTINTABLE_INTERFACE_ID, packedClassFrameInfo, MOCK_METHOD_ID.get().getMethodLabel());

        // make asm prefix to notify us about exception
        asmPrefix = EXCEPTION_PROCESSING_BLOCK;

        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new INVOKEINTERFACE(MOCK_INTERFACE_METHOD_INDEX, args.length + 1), mock(InstructionHandle.class), writer);
        makePostfixWithBreakPoint(TEST_EXPRESSION_4_LABEL, MOCK_CLASS_NAME, TEST_METHOD, args, RESULT_TYPE, writer);

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

        assertEquals("Exception must be thrown", EXCEPTION_FLAG, pop());

        assertElementsOnStack(6);
    }

    private int testExpression(final int a, final int b, final int c, final int d) {
        return a + b - c * d;
    }

    private String makePostfixWithBreakPoint(final String breakPoint, final String className, final String methodName, final Type[] args, final Type result, final StringWriter out) throws Exception {
        final String processingLabel = LabelAndFrameUtils.makeLabelNameForMethod(className, methodName, result, args);
        final String interfaceIdLabel = LabelAndFrameUtils.makeLabelForMethodID(new MethodID(MOCK_INTERFACE_NAME, MOCK_INTERFACE_METHOD_NAME, result, args));

        out.write("JP " + END_LABEL + "\n");
        out.write(interfaceIdLabel + ": EQU "+ MOCK_INTERFACE_METHOD_ID +"\n");
        out.write(processingLabel + ":\n");
        if (breakPoint == null) {
            out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n JP " + END_LABEL + "\n");
        } else {
            out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n" + breakPoint + ": NOP\n RET\n");
        }

        return processingLabel;
    }

    @Override
    public void processBreakPoint(final String label) {
        assertEquals(FAKE_OBJECT_ADDRESS, readLocalFrameVariable(0));
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
            for (int i = 0; i < (localsNumber + 1); i++) {
                writeLocalFrameVariable(i, 0xCACA);
            }
        }
    }

    public String prepareInvokeinterfaceManagerText() {
        return invokeInterfaceManager.replace(MACROS_INVOKEINTERFACE_TABLE, invokeinterfaceTable);
    }

    @Override
    public String getAsmPrefix() {
        return asmPrefix;
    }

    @Override
    public String getAsmPostfix() {
        return athrowManager + "\n" + prepareInvokeinterfaceManagerText() + "\n" + prepareMemoryManagerText();
    }
}
