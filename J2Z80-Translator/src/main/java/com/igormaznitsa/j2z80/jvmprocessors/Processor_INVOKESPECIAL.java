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
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

// class to process INVOKESPECIAL with code 183
public class Processor_INVOKESPECIAL extends AbstractInvokeProcessor implements NeedsMemoryManager {

    private final String template;

    public Processor_INVOKESPECIAL() {
        super();
        template = loadResourceFileAsString("INVOKESPECIAL.a80");
    }

    @Override
    public String getName() {
        return "INVOKESPECIAL";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final INVOKESPECIAL inv = (INVOKESPECIAL) instruction;
        final MethodGen invokingMethod = getInvokedMethod(methodTranslator, inv);

        if (!checkBootstrapCall(methodTranslator, inv, out)) {
            assertMethodIsNotNull(invokingMethod, methodTranslator, inv);

            final String labelForMethod = getMethodLabel(methodTranslator, inv);

            final int argBlockSize = calculateArgumentBlockSize(invokingMethod);
            final int frameSize = calculateTotalFrameSizeWithoutLocals(invokingMethod);

            assertLocalVariablesNumber(invokingMethod);

            final String prefix = generateFramePrefix(argBlockSize, frameSize);
            String postfix = generateFramePostfix(argBlockSize, frameSize);

            if (invokingMethod.getReturnType().getType() != Type.VOID.getType()) {
                postfix += "PUSH BC\n";
            }

            final String res = template.replace(MACROS_ADDRESS, labelForMethod).replace(MACROS_PREFIX, prefix).replace(MACROS_POSTFIX, postfix);

            out.write(res);
            out.write(NEXT_LINE);
        }
    }
}
