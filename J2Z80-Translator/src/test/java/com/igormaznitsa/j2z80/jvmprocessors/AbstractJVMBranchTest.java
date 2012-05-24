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

import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import java.io.StringWriter;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.InstructionHandle;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.powermock.reflect.Whitebox;

public abstract class AbstractJVMBranchTest extends AbstractJvmCommandProcessorTest {
    
    private String prepareTest(final Class<? extends BranchInstruction> instruction, final StringWriter writer) throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction);

        final int TARGET_INSTRUCTION_POS = 123;
        final InstructionHandle mockTarget = mock(InstructionHandle.class);
        when(mockTarget.getPosition()).thenReturn(Integer.valueOf(TARGET_INSTRUCTION_POS));
        final BranchInstruction instructionInstance = instruction.getConstructor(InstructionHandle.class).newInstance(mockTarget);
        final BranchHandle branchHandle = Whitebox.invokeMethod(BranchHandle.class, "getBranchHandle", instructionInstance);
        final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, branchHandle.getTarget().getPosition());
        
        processor.process(CLASS_PROCESSOR_MOCK, instructionInstance, branchHandle, writer);
        return jumpLabel;
    }
    
    public void assertTrueCondition(final Class<? extends BranchInstruction> instruction) throws Exception {
        final StringWriter writer = new StringWriter();
        final String jumpLabel = prepareTest(instruction,writer);

        writer.append("!LOOP: JP !LOOP\n");
        writer.append(jumpLabel+": \n");
        assertLinearExecutionToEnd(writer.toString());
        assertEquals(INIT_SP, SP);
    } 

    public void assertFalseCondition(final Class<? extends BranchInstruction> instruction) throws Exception {
        final StringWriter writer = new StringWriter();
        final String jumpLabel = prepareTest(instruction,writer);

        writer.append("JR !!!FALSEEND\n");
        writer.append(jumpLabel+": JP "+jumpLabel+"\n");
        writer.append("!!!FALSEEND: \n");
        
        assertLinearExecutionToEnd(writer.toString());
        assertEquals(INIT_SP, SP);
    } 
}
