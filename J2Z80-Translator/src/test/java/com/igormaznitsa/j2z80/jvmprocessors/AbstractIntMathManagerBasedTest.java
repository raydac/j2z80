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

import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.aux.Utils;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public abstract class AbstractIntMathManagerBasedTest extends AbstractJvmCommandProcessorTest {

    private static final int EXCEPTION_FLAG_ADDRESS = 0x100;
    private static final int EXCEPTION_FLAG = 0xCA;

    public String prepareForTest(final Instruction instruction) throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());
        final StringWriter writer = new StringWriter();
        processor.process(CLASS_PROCESSOR_MOCK, instruction, mock(InstructionHandle.class), writer);
        return writer.toString();
    }

    @Override
    public String getAsmPostfix() {
        try {
            final String arithblock = Utils.readTextResource(AbstractJvmCommandProcessor.class, "INT_ARITHMETIC_MANAGER.a80");
            final String athrowmanager = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
            return "JP " + END_LABEL + "\n" + arithblock + "\n" + athrowmanager;
        }
        catch (IOException ex) {
            fail("Can't load INT Math code block");
            return null;
        }
    }

    public void assertException(final String asm) {
        final String exceptionCode = "XOR A\n LD (" + EXCEPTION_FLAG_ADDRESS + "),A\n LD HL,ARITH_EXCEPTION\n LD  (" + NeedsATHROWManager.ATHROW_PROCESSING_ADDRESS + "),HL\n"
                + "JP ENDEX\n ARITH_EXCEPTION: POP HL\n LD A," + EXCEPTION_FLAG + "\n LD (" + EXCEPTION_FLAG_ADDRESS + "),A\n JP " + END_LABEL + "\nENDEX:\n";
        
        assertLinearExecutionToEnd(exceptionCode+asm);
        assertEquals("Exception block must be called",EXCEPTION_FLAG,peekb(EXCEPTION_FLAG_ADDRESS));
    }
}
