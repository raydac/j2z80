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

import com.igormaznitsa.j2z80.aux.*;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.*;
import java.util.*;
import org.apache.bcel.generic.*;

public abstract class AbstractJvmCommandProcessor {

    private static final Map<Class<? extends Instruction>, AbstractJvmCommandProcessor> PROCESSORS = new HashMap<Class<? extends Instruction>, AbstractJvmCommandProcessor>();
    public static final String NEXT_LINE = Utils.NEXT_LINE;
    public static final String MACROS_ADDRESS = "%address%";
    public static final String MACROS_ID = "%id%";
    public static final String MACROS_VALUE = "%value%";
    public static final String MACROS_INDEX = "%index%";
    public static final String MACROS_PREFIX = "%prefix%";
    public static final String MACROS_POSTFIX = "%postfix%";
    public static final String MACROS_OBJREFOFFSET = "%objrefoffset%";
    public static final String MACROS_ARGAREALEN = "%argumentarealen%";
    public static final String MACROS_RECORDADDR = "%recordaddress%";

    static {
        try {
            final String PROCESSOR_LIST_FILE = "processorlist.txt";
            final InputStream file = AbstractJvmCommandProcessor.class.getResourceAsStream(PROCESSOR_LIST_FILE);
            Assert.assertNotNull("There must be "+PROCESSOR_LIST_FILE +" in the same directory", file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            try {
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().startsWith(";")) {
                        continue;
                    }
                    final String className = AbstractJvmCommandProcessor.class.getPackage().getName() + '.' + line;
                    final Class<? extends AbstractJvmCommandProcessor> cls = Class.forName(className).asSubclass(AbstractJvmCommandProcessor.class);
                    final AbstractJvmCommandProcessor processor = (AbstractJvmCommandProcessor) cls.newInstance();
                    final Class<? extends Instruction> bcelClass = Class.forName("org.apache.bcel.generic." + processor.getName()).asSubclass(Instruction.class);
                    PROCESSORS.put(bcelClass, processor);
                }
            }
            finally {
                Utils.silentlyClose(reader);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Can't init processors", ex);
        }
    }

    protected String loadResourceFileAsString(final String path) {
        try {
            return Utils.readTextResource(this.getClass(), path);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Can't read resource " + path, ex);
        }
    }

    public static AbstractJvmCommandProcessor findProcessor(final Class<? extends Instruction> instruction) {
        return PROCESSORS.get(instruction);
    }

    public abstract String getName();

    public abstract void process(MethodTranslator classProcessor, Instruction instruction, org.apache.bcel.generic.InstructionHandle handle, Writer out) throws IOException;

    public static int prepareLocalVariableIndex(final int index) {
        final int result = index << 1;

        if (result < 0 || result > 128) {
            throw new IllegalArgumentException("Incompatible local variable index detected [" + index + ']');
        }

        return result;
    }

    public static void assertLocalVariablesNumber(final MethodGen method) {
        if (!method.isInterface()) {
            final String label = method.getClassName() + '#' + method.getName()+" "+method.getSignature();
            final int MAX_VARIABLES = 64;
            final int locals = ( method.isStatic() ? 0 : 1 ) + method.getArgumentTypes().length;
            Assert.assertTrue("Max locals number must be less than "+MAX_VARIABLES+" ["+locals+"] at "+label, locals<MAX_VARIABLES);
        }
    }
}
