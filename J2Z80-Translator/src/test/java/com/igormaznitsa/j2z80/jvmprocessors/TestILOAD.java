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

import java.io.*;
import org.apache.bcel.generic.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TestILOAD extends AbstractJvmCommandProcessorTest {
    
    @Test
    public void testExecution() throws IOException {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ILOAD.class);
        final StringWriter writer = new StringWriter();
        final int VALUE = 0xCAFE;
        final int VAR_INDEX = 43;

        final int IX_ADDRESS = 0x7000;
        IX = IX_ADDRESS;
        
        writeLocalFrameVariable(VAR_INDEX, VALUE);
        
        processor.process(CLASS_PROCESSOR_MOCK, new ILOAD(VAR_INDEX), mock(InstructionHandle.class), writer);
        assertLinearExecutionToEnd(writer.toString());
        
        assertEquals(VALUE,pop());
        assertStackEmpty();
    } 
}
