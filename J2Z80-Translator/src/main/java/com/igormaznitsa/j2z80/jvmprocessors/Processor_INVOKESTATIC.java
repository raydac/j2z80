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
import com.igormaznitsa.j2z80.aux.LabelUtils;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.aux.Utils;
import java.io.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

// class to process INVOKESTATIC with code 184
public class Processor_INVOKESTATIC extends AbstractInvokeProcessor implements NeedsMemoryManager {

    private final String template;

    public Processor_INVOKESTATIC() {
        super();
        template = loadResourceFileAsString("INVOKESTATIC.a80");
    }

    @Override
    public String getName() {
        return "INVOKESTATIC";
    }

    public String [] generateCallForStaticInitalizer(final ClassGen classGen){
        MethodGen initingMethod = null;
        
        for(final Method method : classGen.getMethods()){
            if (method.isStatic() && "<clinit>".equals(method.getName()) && method.getArgumentTypes().length == 0 && method.getReturnType().getType() == Type.VOID.getType()){
                initingMethod = new MethodGen(method, classGen.getClassName(), classGen.getConstantPool());
                break;
            }
        }
        
        if (initingMethod == null){
            return new String[0];
        }
        
        final int argumentMemorySize = calculateArgumentBlockSize(initingMethod);
        final int totalMemorySize = calculateTotalFrameSize(initingMethod);
        String prefix = "";
        String postfix = "";


        final String labelForMethod = LabelUtils.makeLabelNameForMethod(initingMethod);

        final boolean needsFrame = argumentMemorySize != 0 || totalMemorySize != 0;

        assertLocalVariablesNumber(initingMethod);

        if (needsFrame) {
            prefix = generateFramePrefix(argumentMemorySize, totalMemorySize);
            postfix = generateFramePostfix(argumentMemorySize, totalMemorySize);
        }

        final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix)+'\n';

        return Utils.breakToLines(res);
    }
    
    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final INVOKESTATIC inv = (INVOKESTATIC) instruction;

        final MethodGen invokedMethod = getInvokedMethod(methodTranslator, inv);

        if (!checkBootstrapCall(methodTranslator, inv, out)) {
            assertMethodIsNotNull(invokedMethod, methodTranslator, inv);

            final int argumentMemorySize = calculateArgumentBlockSize(invokedMethod);
            final int totalMemorySize = calculateTotalFrameSize(invokedMethod);
            String prefix = "";
            String postfix = "";


            final String labelForMethod = getMethodLabel(methodTranslator, inv);

            final boolean needsFrame = argumentMemorySize != 0 || totalMemorySize != 0;

            assertLocalVariablesNumber(invokedMethod);

            if (needsFrame) {
                prefix = generateFramePrefix(argumentMemorySize, totalMemorySize);
                postfix = generateFramePostfix(argumentMemorySize, totalMemorySize);
            }

            if (invokedMethod.getReturnType().getType() != Type.VOID.getType()) {
                postfix += "PUSH BC\n";
            }

            final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix);

            out.write(res);
            out.write(NEXT_LINE);
        }
    }
}
