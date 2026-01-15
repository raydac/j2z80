/* 
 * Copyright 2019 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.meta.common.utils.Assertions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
  // the map contains all processors for allowed jvm commands
  private static final Map<Class<? extends Instruction>, AbstractJvmCommandProcessor> PROCESSORS = new HashMap<>();

  static {
    try {
      // read the file containing all jvm commands, search processors and map them
      final String PROCESSOR_LIST_FILE = "processorlist.txt";
      final InputStream file = AbstractJvmCommandProcessor.class.getResourceAsStream(PROCESSOR_LIST_FILE);
      Assertions.assertNotNull("There must be " + PROCESSOR_LIST_FILE + " in the same directory", file);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8));
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
          final AbstractJvmCommandProcessor processor = cls.getDeclaredConstructor().newInstance();
          final Class<? extends Instruction> bcelClass = Class.forName("org.apache.bcel.generic." + processor.getName()).asSubclass(Instruction.class);
          PROCESSORS.put(bcelClass, processor);
        }
      } finally {
        Utils.silentlyClose(reader);
      }
    } catch (Exception ex) {
      throw new Error("Can't init processors", ex);
    }
  }

  /**
   * Find process for a JVM instruction
   *
   * @param instruction a jvm instruction, must not be null
   * @return found processor for the instruction or null if it is not supported
   * @see org.apache.bcel.generic.Instruction
   */
  public static AbstractJvmCommandProcessor findProcessor(final Class<? extends Instruction> instruction) {
    return PROCESSORS.get(instruction);
  }

  /**
   * Common auxiliary method to calculate local variable offset in the method stack frame.
   *
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
   *
   * @param method a method to be checked, must not be null
   */
  public static void assertLocalVariablesNumber(final MethodGen method) {
    if (!method.isInterface()) {
      final String label = method.getClassName() + '#' + method.getName() + " " + method.getSignature();
      final int MAX_VARIABLES = 64;
      final int locals = (method.isStatic() ? 0 : 1) + method.getArgumentTypes().length;
      Assertions.assertTrue("Max locals number for a mathod must be less than " + MAX_VARIABLES + " [" + locals + "] at " + label, locals < MAX_VARIABLES);
    }
  }

  /**
   * Inside method allows a processor to read resources places in its class path
   *
   * @param path the resource path to be read, must nt be null
   * @return read resource as String
   * @throws IllegalArgumentException it will be thrown if the resource is not found or can't be read
   */
  protected String loadResourceFileAsString(final String path) {
    try {
      return Utils.readTextResource(this.getClass(), path);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Can't read resource " + path, ex);
    }
  }

  /**
   * Get the name of the instruction processed by the class.
   *
   * @return the instruction name as String
   */
  public abstract String getName();

  /**
   * Process an instruction allowed by the processor
   *
   * @param methodTranslator a translator translating the method, must not be null
   * @param instruction      an instruction to be processed, must not be null
   * @param handle           the instruction handle for the processing instruction, must not be null
   * @param out              the writer to out the result, must not be null
   * @throws IOException it will be thrown if there is any problem during processing
   */
  public abstract void process(MethodTranslator methodTranslator, Instruction instruction, org.apache.bcel.generic.InstructionHandle handle, Writer out) throws IOException;
}
