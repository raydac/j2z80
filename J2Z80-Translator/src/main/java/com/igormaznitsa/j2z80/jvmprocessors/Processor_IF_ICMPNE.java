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

// class to process IF_ICMPNE with code 160
public class Processor_IF_ICMPNE extends AbstractJvmCommandProcessor {
    private final String template; 
    
    public Processor_IF_ICMPNE(){
        super();
        template = loadResourceFileAsString("IF_ICMPNE.a80");
    }

    @Override
    public String getName() {
        return "IF_ICMPNE";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final IF_ICMPNE ificmpne = (IF_ICMPNE)instruction;
        final String label = LabelUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), ( (BranchHandle) handle ).getTarget().getPosition());
        out.write(template.replace(MACROS_ADDRESS, label));
        out.write(NEXT_LINE);
    }
}
