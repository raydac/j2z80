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
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.TABLESWITCH;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public abstract class AbstractJVMSelectTest extends AbstractJvmCommandProcessorTest {

    public static final int DEFAULT_POSITION = 0xFFFF;
    
    public void executeSelectInstruction(final Class<? extends Select> instruction, final int checkValue, final int[] allowedIndexes) throws Exception {
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction);

        int checkedIndex = -1;
        
        for(int i=0;i<allowedIndexes.length;i++){
            if (allowedIndexes[i] == checkValue){
                checkedIndex = i;
            }
        }
        
        final InstructionHandle[] targets = mockTargets(allowedIndexes);
        final InstructionHandle defaultTarget = makeTargetMock(0xFFFF);

        Select instance = null;
        if (instruction == TABLESWITCH.class) {
            instance = new TABLESWITCH(allowedIndexes, targets, defaultTarget);
        } else if (instruction == LOOKUPSWITCH.class) {
            instance = new LOOKUPSWITCH(allowedIndexes, targets, defaultTarget);
        } else {
            throw new IllegalArgumentException("Unsupported select instruction");
        }
        
        final StringWriter out = new StringWriter();
        
        final BranchHandle mockInstructionHandle = Whitebox.invokeMethod(BranchHandle.class,"getBranchHandle",instance);
        
        processor.process(CLASS_PROCESSOR_MOCK, instance, mockInstructionHandle, out);
        
        for(int i=0;i<targets.length;i++){
            final String label = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, targets[i].getPosition());
            final String asm = makeAsmStubForIndex(targets[i], allowedIndexes[i], label);
            out.append(asm);
        }
        
        out.append(makeAsmStubForIndex(defaultTarget, DEFAULT_POSITION, LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, defaultTarget.getPosition())));
        
        push(checkValue);
        
        assertLinearExecutionToEnd(out.toString());
    }

    private String makeAsmStubForIndex(final InstructionHandle handle, final int data, final String defaultLabel)
    {
        final StringBuilder buffer = new StringBuilder();
        
            final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(CLASS_GEN_MOCK, mockupOfInvokedMethod, handle.getPosition());
            buffer.append(jumpLabel+":\n");
            buffer.append("LD HL,").append(data).append("\n");
            buffer.append("PUSH HL\n");
            buffer.append("JP ").append(END_LABEL).append("\n");
        
        return buffer.toString();
    }
    
    private InstructionHandle makeTargetMock(final int position) throws Exception {
        
        final InstructionHandle handle = Mockito.spy((InstructionHandle)Whitebox.invokeMethod(InstructionHandle.class, "getInstructionHandle",new LDC(1)));
        Mockito.doReturn(position).when(handle).getPosition();
        Mockito.doReturn(Boolean.TRUE).when(handle).hasTargeters();
        return handle;
    }

    private InstructionHandle[] mockTargets(final int[] values) throws Exception {
        final InstructionHandle[] result = new InstructionHandle[values.length];

        int index = 0;
        int position = 100;
        for (final int value : values) {
            final InstructionHandle handle = makeTargetMock(position);
            position += 10;
            result[index++] = handle;
        }

        return result;
    }
}
