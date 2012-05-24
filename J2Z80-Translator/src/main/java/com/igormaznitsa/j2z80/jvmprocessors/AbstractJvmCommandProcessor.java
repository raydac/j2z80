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

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.MethodGen;

/**
 * The Class describes an abstract processor of a JVM command.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractJvmCommandProcessor {

    // the map contains all processors for allowed jvm commands
    private static final Map<Class<? extends Instruction>, AbstractJvmCommandProcessor> PROCESSORS = new HashMap<Class<? extends Instruction>, AbstractJvmCommandProcessor>();
    /**
     * The next line constant
     */
    public static final String NEXT_LINE = Utils.NEXT_LINE;
    /**
     * The macros name to be used for an address replacement
     */
    public static final String MACROS_ADDRESS = "%address%";
    /**
     * The macros name to be used for an id replacement
     */
    public static final String MACROS_ID = "%id%";
    /**
     * The macros name to be used for a value replacement
     */
    public static final String MACROS_VALUE = "%value%";
    /**
     * The macros name to be used for an index replacement
     */
    public static final String MACROS_INDEX = "%index%";
    /**
     * The macros name to be used for a prefix text replacement
     */
    public static final String MACROS_PREFIX = "%prefix%";
    /**
     * The macros name to be used for a postfix text replacement
     */
    public static final String MACROS_POSTFIX = "%postfix%";
    /**
     * The macros name to be used for a object reference offset replacement
     */
    public static final String MACROS_OBJREFOFFSET = "%objrefoffset%";
    /**
     * The macros name to be used to replace an argument area length
     */
    public static final String MACROS_ARGAREALEN = "%argumentarealen%";
    /**
     * The macros name to be used to replace a record address address
     */
    public static final String MACROS_RECORDADDR = "%recordaddress%";

    static {
        try {
            // read the file containig all jvm commands, search processors and map them
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

    /**
     * Inside method allows a processor to read resources places in its class path
     * @param path the resource path to be read, must nt be null
     * @return read resource as String
     * @throws IllegalArgumentException it will be thrown if the resource is not found or can't be read
     */
    protected String loadResourceFileAsString(final String path) {
        try {
            return Utils.readTextResource(this.getClass(), path);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Can't read resource " + path, ex);
        }
    }

    /**
     * Find process for a JVM instruction
     * @param instruction a jvm instruction, must not be null
     * @return found processor for the instruction or null if it is not supported
     * @see org.apache.bcel.generic.Instruction
     */
    public static AbstractJvmCommandProcessor findProcessor(final Class<? extends Instruction> instruction) {
        return PROCESSORS.get(instruction);
    }

    /**
     * Get the name of the instruction processed by the class.
     * @return the instruction name as String
     */
    public abstract String getName();

    /**
     * Process an instruction allowed by the processor
     * @param methodTranslator a translator translating the method, must not be null
     * @param instruction an instruction to be processed, must not be null
     * @param handle the instruction handle for the processing instruction, must not be null
     * @param out the writer to out the result, must not be null 
     * @throws IOException it will be thrown if there is any problem during processing
     */
    public abstract void process(MethodTranslator methodTranslator, Instruction instruction, org.apache.bcel.generic.InstructionHandle handle, Writer out) throws IOException;

    /**
     * Common auxiliary method to calculate local variable offset in the method stack frame.
     * @param index the local variable index
     * @return calculated offset
     */
    public static int prepareLocalVariableIndex(final int index) {
        final int result = index << 1;

        if (result < 0 || result > 128) {
            throw new IllegalArgumentException("Incompatible local variable index detected [" + index + ']');
        }

        return result;
    }

    /**
     * Common auxiliary method to check number of local variables in a method, method must have number of local variables less than 64.
     * @param method a method to be checked, must not be null
     */
    public static void assertLocalVariablesNumber(final MethodGen method) {
        if (!method.isInterface()) {
            final String label = method.getClassName() + '#' + method.getName()+" "+method.getSignature();
            final int MAX_VARIABLES = 64;
            final int locals = ( method.isStatic() ? 0 : 1 ) + method.getArgumentTypes().length;
            Assert.assertTrue("Max locals number for a mathod must be less than "+MAX_VARIABLES+" ["+locals+"] at "+label, locals<MAX_VARIABLES);
        }
    }
}
