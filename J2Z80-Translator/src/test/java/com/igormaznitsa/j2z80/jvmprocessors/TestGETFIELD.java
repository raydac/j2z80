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

import com.igormaznitsa.j2z80.aux.LabelUtils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.*;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TestGETFIELD extends AbstractJvmCommandProcessorTest {

    private final static String CLASS_NAME = "some.class.test.tttt";
    private final static String FIELD_NAME = "Field";
    private final static Type   FIELD_TYPE = Type.INT;
    
    private final static int CP_INDEX_CLASS_NAME = CONSTANT_USER_DEFINED+1;
    private final static int CP_INDEX_FIELD_NAME = CONSTANT_USER_DEFINED+2;
    private final static int CP_INDEX_FIELD_NAMEANDTYPE = CONSTANT_USER_DEFINED+3;
    private final static int CP_INDEX_CLASSREF = CONSTANT_USER_DEFINED+4;
    private final static int CP_INDEX_FIELDREF = CONSTANT_USER_DEFINED+5;
    private final static int CP_INDEX_FIELD_SIGNATURE = CONSTANT_USER_DEFINED+6;
    
    private final static int VALUE = 0xCAFE;
    private final static int OBJECT_ADDRESS = 0x8000;
    private final static int FIELD_OFFSET = 1103;
    
    @Test(timeout=3000L)
    public void testExecution() throws IOException {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(GETFIELD.class);
        final StringWriter writer = new StringWriter();
        

        final ConstantUtf8 cp_className =  new ConstantUtf8(CLASS_NAME);
        final ConstantUtf8 cp_fieldName = new ConstantUtf8(FIELD_NAME);
        final ConstantClass cp_class = new ConstantClass(CP_INDEX_CLASS_NAME);
        final ConstantNameAndType cp_nameandtype = new ConstantNameAndType(CP_INDEX_FIELD_NAME,CP_INDEX_FIELD_SIGNATURE);
        final ConstantUtf8 cp_fieldtype = new ConstantUtf8(FIELD_TYPE.getSignature());
        final ConstantFieldref cp_fieldref = new ConstantFieldref(CP_INDEX_CLASSREF,CP_INDEX_FIELD_NAMEANDTYPE);
        
        when(CP_GEN_MOCK.getConstant(CP_INDEX_CLASSREF)).thenReturn(cp_class);
        when(CP_GEN_MOCK.getConstant(CP_INDEX_CLASS_NAME)).thenReturn(cp_className);
        when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELDREF)).thenReturn(cp_fieldref);
        when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_NAME)).thenReturn(cp_fieldName);
        when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_NAMEANDTYPE)).thenReturn(cp_nameandtype);
        when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_SIGNATURE)).thenReturn(cp_fieldtype);
        
        when(CP_MOCK.getConstantString(CP_INDEX_CLASSREF,Constants.CONSTANT_Class)).thenReturn(CLASS_NAME);
        
        final String staticFieldLabel = LabelUtils.makeLabelNameForField(CLASS_NAME, FIELD_NAME, FIELD_TYPE);
        
        final GETFIELD testInstruction = new GETFIELD(CP_INDEX_FIELDREF);
        
        processor.process(CLASS_PROCESSOR_MOCK,testInstruction, mock(InstructionHandle.class), writer);
        
        pokew(OBJECT_ADDRESS+FIELD_OFFSET, VALUE);
        
        push(OBJECT_ADDRESS);
        final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());
        assertEquals(VALUE, pop());
        assertStackEmpty();
        
        assertEquals(VALUE,peekw(OBJECT_ADDRESS+FIELD_OFFSET));
    }

    @Override
    public String getAsmPostfix() {
        final String fieldOffset = LabelUtils.makeLabelNameForFieldOffset(CLASS_NAME, FIELD_NAME, FIELD_TYPE);
        return fieldOffset+": EQU "+FIELD_OFFSET;
    }
    
    
}
