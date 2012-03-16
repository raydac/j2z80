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

// class to process JSR_W with code 201
public class Processor_JSR_W extends AbstractJvmCommandProcessor {
    private final String template; 
    
    public Processor_JSR_W(){
        super();
        template = loadResourceFileAsString("JSR_W.a80");
    }

    @Override
    public String getName() {
        return "JSR_W";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final JSR_W jsrw = (JSR_W)instruction;
        final String label = LabelUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(),((BranchHandle)handle).getTarget().getPosition());
        out.write(template.replace(MACROS_ADDRESS, label));
        out.write(NEXT_LINE);
    }
}
