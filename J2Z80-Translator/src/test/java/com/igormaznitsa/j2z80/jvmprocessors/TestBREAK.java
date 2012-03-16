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

import com.igormaznitsa.j2z80.api.additional.NeedsBREAKPOINTManager;
import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.*;
import org.apache.bcel.generic.*;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TestBREAK extends AbstractJvmCommandProcessorTest {
    private static final int VALUE = 0xCAFE;
    private static final int TEST_ADDRESS = 258;
    private static final String BREAK_POINT_START_ADDRESS = "BP_START_LABEL";
    
    @Test(timeout=3000L)
    public void testExecution() throws IOException {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(BREAKPOINT.class);
        final StringWriter writer = new StringWriter();
        
        processor.process(CLASS_PROCESSOR_MOCK, new BREAKPOINT(), mock(InstructionHandle.class), writer);
        final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());
        
        assertEquals(asm.findLabelAddress(BREAK_POINT_START_ADDRESS).intValue(),peekw(TEST_ADDRESS));
        assertStackEmpty();
    }

    @Override
    public String getAsmPrefix() {
        return "LD HL,BP_PROCESSOR\nLD ("+NeedsBREAKPOINTManager.BREAKPOINT_PROCESSING_SUB_ADDRESS+"),HL\n "+BREAK_POINT_START_ADDRESS+":\n";
    }

    @Override
    public String getAsmPostfix() {
        try {
            final String str = Utils.readTextResource(AbstractJvmCommandProcessor.class, "BREAKPOINT_MANAGER.a80");
            return "JR "+END_LABEL+"\n BP_PROCESSOR: LD ("+TEST_ADDRESS+"),HL\n RET\n"+str;
        }catch(IOException ex){
            fail("IOException, can't read text resource");
            return null;
        }
    }
 
}
