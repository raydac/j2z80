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
import java.io.StringWriter;
import org.apache.bcel.generic.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TestLDC extends AbstractJvmCommandProcessorTest {
    
    @Test
    public void testInt() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,new LDC(CONSTANT_INT), mock(InstructionHandle.class), writer);
        assertLinearExecutionToEnd(writer.toString());
        assertEquals(ETALON_CONSTANT_INTEGER, (short)pop());
        assertStackEmpty();

        verify(CLASS_PROCESSOR_MOCK,never()).registerUsedConstantPoolItem(CONSTANT_INT);
    }
    
    @Test
    public void testUTF8() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        
        final String etalonLabel = LabelAndFrameUtils.makeLabelForConstantPoolItem(JCLASS_GEN_MOCK, CONSTANT_UTF8);
        when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_UTF8)).thenReturn(etalonLabel);
        
        processor.process(CLASS_PROCESSOR_MOCK, new LDC(CONSTANT_UTF8), mock(InstructionHandle.class), writer);
        
        final String labelName = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);
        final int labelAddress = assertLinearExecutionToEnd(writer.toString()).findLabelAddress(labelName).intValue();
        assertEquals(labelAddress, pop());
        assertStackEmpty();

        verify(CLASS_PROCESSOR_MOCK).registerUsedConstantPoolItem(CONSTANT_UTF8);
    }

    @Test
    public void testString() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        
        final String labelName = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);

        when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_UTF8)).thenReturn(labelName);
        when(CLASS_PROCESSOR_MOCK.registerUsedConstantPoolItem(CONSTANT_STR)).thenReturn(labelName);

        processor.process(CLASS_PROCESSOR_MOCK,new LDC(CONSTANT_STR), mock(InstructionHandle.class), writer);
        
        final int labelAddress = assertLinearExecutionToEnd(writer.toString()).findLabelAddress(labelName).intValue();
        assertEquals(labelAddress, pop());
        assertStackEmpty();

        verify(CLASS_PROCESSOR_MOCK,never()).registerUsedConstantPoolItem(CONSTANT_UTF8);
        verify(CLASS_PROCESSOR_MOCK).registerUsedConstantPoolItem(CONSTANT_STR);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDouble() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,new LDC(CONSTANT_DOUBLE), mock(InstructionHandle.class), writer);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFloat() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,new LDC(CONSTANT_FLOAT), mock(InstructionHandle.class), writer);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLong() throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(LDC.class);
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK,new LDC(CONSTANT_LONG), mock(InstructionHandle.class), writer);
    }

     @Override
    public String getAsmPostfix() {
        final String labelUTF8 = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_UTF8);
        final String labelStr = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_STR);
        final String labelInt = LabelAndFrameUtils.makeLabelForConstantPoolItem(CLASS_GEN_MOCK.getJavaClass(), CONSTANT_INT);
        return labelUTF8+": NOP\n"+labelInt+": NOP\n"+labelStr+": EQU "+labelUTF8;
    }
}
