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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.powermock.reflect.Whitebox;

public abstract class AbstractTestBasedOnMemoryManager extends AbstractJvmCommandProcessorTest implements NeedsMemoryManager {

    protected final static String INSTANCEOFMANAGER_TEXT;
    protected final static String MEMORYMANAGER_TEXT;

    static {
        try {
            MEMORYMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "MEMORY_MANAGER.a80");
        }
        catch (IOException ex) {
            throw new Error("Can't read memory manager assembler text");
        }
        try {
            INSTANCEOFMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "INSTANCEOF_MANAGER.a80");
        }
        catch (IOException ex) {
            throw new Error("Can't read instanceof manager assembler text");
        }
    }
    private int memoryTopAddress;
    private int memoryPointerAddress;

    @Before
    @Override
    public void beforeTest() {
        SP = INIT_SP;
        PC = START_ADDRESS;
    }

    public String prepareMemoryManagerText() {
        return MEMORYMANAGER_TEXT;
    }
    
    private String generateAsmForAllocateCommand(final Instruction[] instructions) throws Exception {
        final StringWriter out = new StringWriter();

        for (final Instruction instruction : instructions) {
            final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());
            final InstructionHandle mockInstructionHandle = Whitebox.invokeMethod(InstructionHandle.class, "getInstructionHandle", instruction);
            processor.process(CLASS_PROCESSOR_MOCK, instruction, mockInstructionHandle, out);
        }
        return out.toString();
    }

    public int getInitialMemoryAddress() {
        return memoryTopAddress;
    }

    public int getAllocatedMemorySize() {
        return peekw(memoryPointerAddress) - memoryTopAddress;
    }

    public Z80Asm assertLinearExecutionToEnd(final String text, final int assertedMemory) {
        final String [] asmText = new String[]{
                    getAsmBeforeAll(),
                    "   org " + START_ADDRESS,        
                    getAsmPrefix(),
                    text,
                    END_LABEL + ": NOP",
                    getAsmPostfix(),
                    prepareMemoryManagerText()
                };
        
        for(final String str : asmText){
            System.out.println(str);
        }
        
        final Z80Asm asm = new Z80Asm(asmText);

        final byte[] compiled = asm.process();

        System.arraycopy(compiled, 0, memory, asm.getDataOffset(), compiled.length);

        memoryTopAddress = asm.findLabelAddress(MEMORY_HEAP_START_AREA_LABEL).intValue();
        memoryPointerAddress = asm.findLabelAddress(VAR_MANAGER_TOP_POINTER).intValue();

        endAddress = asm.findLabelAddress(END_LABEL).intValue();
        exec(Integer.MAX_VALUE);
        assertEquals("Must allocate block size", assertedMemory, getAllocatedMemorySize());
        return asm;
    }

    
    
    public void assertAllocateCommand(final Instruction[] allocateInstruction, int assertedMemory) throws Exception {
        final String textForCommand = generateAsmForAllocateCommand(allocateInstruction);

        final String [] asmText = new String[]{
                    getAsmBeforeAll(),
                    "   org " + START_ADDRESS,
                    getAsmPrefix(),
                    textForCommand,
                    END_LABEL + ": NOP",
                    getAsmPostfix(),
                    prepareMemoryManagerText()
                };
        
        for(final String str : asmText){
            System.out.println(str);
        }
        
        final Z80Asm asm = new Z80Asm(asmText);

        final byte[] compiled = asm.process();

        System.arraycopy(compiled, 0, memory, asm.getDataOffset(), compiled.length);

        memoryTopAddress = asm.findLabelAddress(MEMORY_HEAP_START_AREA_LABEL).intValue();
        memoryPointerAddress = asm.findLabelAddress(VAR_MANAGER_TOP_POINTER).intValue();

        endAddress = asm.findLabelAddress(END_LABEL).intValue();
        exec(Integer.MAX_VALUE);
        assertEquals("Must allocate block size", assertedMemory, getAllocatedMemorySize());
    }
}
