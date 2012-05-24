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

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.aux.Utils;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.bcel.generic.Type;

public class AbstractInvokeTest extends AbstractJvmCommandProcessorTest implements NeedsMemoryManager, NeedsInstanceofManager {

    public static final Type [] ARGS_NULL = new Type[0];
    static final Type [] ARGS_FOUR_INT = new Type[]{Type.INT, Type.INT, Type.INT, Type.INT};
    
    public static final String TEST_INVOKED_CLASS = "com.invoked.someclass";
    public static final String TEST_INVOKED_METHOD = "someInvokedMethod";
    public static final int FLAG_ADDRESS = 0x100;
    public static final int FLAG_METHOD_CALLED = 0xFE;
    
    public static final int INITIAL_IX = 0xCAFE;
    
    protected static String managerAsmText;

    static {
        try {
            managerAsmText = Utils.readTextResource(AbstractJvmCommandProcessor.class, "MEMORY_MANAGER.a80");
        }
        catch (IOException ex) {
            throw new Error("Can't read memory manager assembler text");
        }
    }

    public String prepareMemoryManagerText() {
        return managerAsmText.replace(MACRO_INSTANCEOFTABLE, "DEFB 0");
    }

    @Override
    public String getAsmPostfix() {
        return prepareMemoryManagerText();
    }

    public void printStackState(final int depth) {
        int sp = SP;
        for (int i = 0; i < depth; i++) {
            final int value = peekw(sp);
            System.out.println("SP: " + Utils.intToString(sp)+" -> " + Utils.intToString(value));
            sp += 2;
        }
    }

    protected void makePostfixWithBreakPoint(final String breakPoint, final StringWriter out) throws Exception {
        final String processingLabel = LabelAndFrameUtils.makeLabelNameForMethod(mockupOfInvokedMethod.getClassName(), mockupOfInvokedMethod.getName(), mockupOfInvokedMethod.getReturnType(), mockupOfInvokedMethod.getArgumentTypes());

        out.write("JP " + END_LABEL + "\n");
        out.write(processingLabel + ":\n");
        if (breakPoint == null) {
            out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n JP " + END_LABEL + "\n");
        } else {
            out.write("LD A," + FLAG_METHOD_CALLED + "\n LD (" + FLAG_ADDRESS + "),A\n" + breakPoint + ": NOP\n RET\n");
        }
    }
}
