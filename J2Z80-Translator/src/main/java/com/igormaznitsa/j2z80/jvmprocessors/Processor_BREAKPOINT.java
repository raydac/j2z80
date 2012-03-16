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

import com.igormaznitsa.j2z80.api.additional.NeedsBREAKPOINTManager;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.*;
import org.apache.bcel.generic.*;

// class to process BREAKPOINT with code 202
public class Processor_BREAKPOINT extends AbstractJvmCommandProcessor implements NeedsBREAKPOINTManager {
    private final String template; 
    
    public Processor_BREAKPOINT(){
        super();
        template = loadResourceFileAsString("BREAKPOINT.a80").replace(MACROS_ADDRESS, BREAKPOINT_PROCESSING_MANAGER);
    }

    @Override
    public String getName() {
        return "BREAKPOINT";
    }

    @Override
    public void process(final MethodTranslator classProcessor, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        classProcessor.getTranslatorContext().getLogger().logWarning("a BREAKPOINT has been met");
        final BREAKPOINT breakpoint = (BREAKPOINT)instruction;
        out.write(template);
        out.write(NEXT_LINE);
    }
}
