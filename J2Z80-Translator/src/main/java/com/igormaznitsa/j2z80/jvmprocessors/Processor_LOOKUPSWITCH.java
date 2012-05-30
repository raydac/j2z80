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
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.*;
import java.util.Locale;
import org.apache.bcel.generic.*;

// class to process LOOKUPSWITCH with code 171
public class Processor_LOOKUPSWITCH extends AbstractJvmCommandProcessor {

    
    private final String template; 
    
    public Processor_LOOKUPSWITCH(){
        super();
        template = loadResourceFileAsString("LOOKUPSWITCH.a80");
    }

    @Override
    public String getName() {
        return "LOOKUPSWITCH";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final LOOKUPSWITCH lookupswitch = (LOOKUPSWITCH)instruction;
        
        final int[] matchs = lookupswitch.getMatchs();
        final InstructionHandle[] targets = lookupswitch.getTargets();

        final InstructionHandle defaultTarget = lookupswitch.getTarget();
        final String defaultJump = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), defaultTarget.getPosition());
                
        out.write(template
                .replace(MACROS_ADDRESS, defaultJump)
                .replace(MACROS_VALUE, Integer.toString(targets.length))
                );

        if (matchs.length > 0) {
            for (int branchIndex = 0; branchIndex < matchs.length; branchIndex++) {
                final int match = matchs[branchIndex];
                final InstructionHandle target = targets[branchIndex];
                final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), target.getPosition());

                out.write("DEFW #"+Integer.toHexString(match & 0xFFFF).toUpperCase(Locale.ENGLISH)+"\n");
                out.write("DEFW "+jumpLabel+"\n");
            }
        }

        out.write(NEXT_LINE);
    }
}
