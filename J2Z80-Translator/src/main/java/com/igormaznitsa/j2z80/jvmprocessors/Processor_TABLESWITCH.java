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

import com.igormaznitsa.j2z80.aux.LabelUtils;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.*;
import org.apache.bcel.generic.*;

// class to process TABLESWITCH with code 170
public class Processor_TABLESWITCH extends AbstractJvmCommandProcessor {

    private static final String MACROS_LOW_IDEX = "%lowindex%";
    private static final String MACROS_HIGH_IDEX = "%highindex%";
    private static final String MACROS_DEFAULT = "%default%";
    
    private final String template;

    public Processor_TABLESWITCH() {
        super();
        template = loadResourceFileAsString("TABLESWITCH.a80");
    }

    @Override
    public String getName() {
        return "TABLESWITCH";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final TABLESWITCH tableswitch = (TABLESWITCH) handle.getInstruction();

        final int[] matchs = tableswitch.getMatchs();
        final InstructionHandle[] targets = tableswitch.getTargets();

        final InstructionHandle defaultTarget = tableswitch.getTarget();
        final String defaultJump = LabelUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), defaultTarget.getPosition());
                
        final int lowIndex = matchs[0];
        final int highIndex = matchs[matchs.length-1];
        
        out.write(template
                .replace(MACROS_LOW_IDEX, Integer.toString(lowIndex))
                .replace(MACROS_HIGH_IDEX, Integer.toString(highIndex))
                .replace(MACROS_DEFAULT, defaultJump)
                );

        if (matchs.length > 0) {
            for (int branchIndex = 0; branchIndex < matchs.length; branchIndex++) {
                final InstructionHandle target = targets[branchIndex];
                final String jumpLabel = LabelUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), target.getPosition());

                out.write("DEFW "+jumpLabel+"\n");
            }
        }

        out.write(NEXT_LINE);
    }
}
