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

import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TestARETURN extends AbstractJvmCommandProcessorTest {
    
    @Test(timeout=3000L)
    public void testExecution() throws IOException {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(ARETURN.class);
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, new ARETURN(), mock(InstructionHandle.class), writer);

        assertLinearExecutionToEnd("ld hl,"+END_LABEL+"\n push hl\n ld hl,#1234\n push hl\n"+writer.toString());
        
        assertEquals(0x1234, BC());
        assertStackEmpty();
    }
    
}
