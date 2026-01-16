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

import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelNameForMethod;

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * The class is the ancestor for all invoke command processors.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractInvokeProcessor extends AbstractJvmCommandProcessor
    implements NeedsMemoryManager {

  /**
   * Calculate the size of memory block in bytes to keep arguments for a method.
   *
   * @param argNumber the number of arguments needed by the method
   * @param isStatic  the flag shows that the method is a static one if it is true
   * @return the memory block size in bytes
   */
  public static int calculateArgumentBlockSize(final int argNumber, final boolean isStatic) {
    int num = argNumber;
    if (!isStatic) {
      num++;
    }
    return num << 1;
  }

  /**
   * Calculate an argument block size in byte for a method
   *
   * @param invokingMethod a method to be used for calculation, must not be null
   * @return the memory block size in bytes
   */
  public static int calculateArgumentBlockSize(final MethodGen invokingMethod) {
    return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length,
        invokingMethod.isStatic());
  }

  /**
   * Calculate the offset to an object reference on the stack.
   *
   * @param argumentBlockSize the argument block size in bytes
   * @return the offset in the block to the object reference
   */
  public static int calculateObjectOffsetOnStack(final int argumentBlockSize) {
    return argumentBlockSize - 2;
  }

  /**
   * Calculate whole stack frame size in bytes for a method ignoring local variables.
   *
   * @param invokingMethod a method to be used for calculations, must not be null
   * @return the whole memory frame size in bytes
   */
  public static int calculateTotalFrameSizeWithoutLocals(final MethodGen invokingMethod) {
    return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length,
        invokingMethod.isStatic());
  }

  /**
   * Generate the prefix for a method invocation
   *
   * @param argMemorySize the memory block size for arguments of the method
   * @param frameMemSize  the stack frame needed by the method
   * @return the string containing the prefix code for the method invocation
   */
  public static String generateFramePrefix(final int argMemorySize, final int frameMemSize) {
    return "LD A," + argMemorySize + NEXT_LINE
        + "LD BC," + frameMemSize + NEXT_LINE
        + "CALL " + SUB_BEFORE_INVOKE + NEXT_LINE;
  }

  /**
   * Generate the postfix for a method invocation
   *
   * @param argMemorySize the memory block size for arguments of the method
   * @param frameMemSize  the stack frame needed by the method
   * @return the string containing the postfix code for the method invocation
   */
  public static String generateFramePostfix(final int argMemorySize, final int frameMemSize) {
    return "CALL " + SUB_AFTER_INVOKE + NEXT_LINE;
  }

  /**
   * Calculate whole stack frame size in bytes based on max local variable number data.
   *
   * @param args      the number of arguments for a method
   * @param maxLocals the number of local variables for a method
   * @param isStatic  the flag shows that the method is a static one if it is true
   * @return the memory frame size in bytes
   */
  public int calculateTotalFrameSizeWithLocals(final int args, final int maxLocals,
                                               final boolean isStatic) {
    int argNumber = args;

    if (!isStatic) {
      argNumber++;
    }

    if (argNumber > maxLocals) {
      throw new IllegalStateException("Frame size is less than arguments number");
    }

    return maxLocals << 1;
  }

  /**
   * Check the invoke instruction for a bootstrap class and if the invoked class is a bootstrap one then the method will process it by a special way.
   *
   * @param methodTranslator a method translator called the method, must not be null
   * @param instruction      an invoke instruction to be checked, must not be null
   * @param bootstrapClassLoader bootstrap class loader, must not be null
   * @param out              the output stream to write commands
   * @return true if the instruction invokes a bootstrap class and it has been processed by the method, else false
   * @throws IOException it will be thrown if any transport problem in the method
   */
  protected boolean isBootstrapCall(
      final MethodTranslator methodTranslator,
      final InvokeInstruction instruction,
      final ClassLoader bootstrapClassLoader,
      final Writer out
  ) throws IOException {
    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    final ObjectType objType = this.getObjectType(methodTranslator, instruction);
    final String methodName = instruction.getMethodName(constantPool);
    final Type[] methodArgs = instruction.getArgumentTypes(constantPool);
    final Type methodResult = instruction.getReturnType(constantPool);

    final MethodGen methodGen = methodTranslator.getTranslatorContext().getMethodContext()
        .findMethod(new MethodID(objType.getClassName(), methodName, methodResult, methodArgs));

    if (methodGen != null) {
      return false;
    }

    final String className = objType.getClassName();
    final AbstractBootstrapClass processor =
        AbstractBootstrapClass.findProcessor(className, bootstrapClassLoader);

    boolean result = false;
    if (processor != null) {
      final boolean isStaticCall = instruction instanceof INVOKESPECIAL;
      final int argAreaSize = calculateArgumentBlockSize(methodArgs.length, isStaticCall);
      final int totalFrameSize = calculateTotalFrameSizeWithLocals(methodArgs.length,
          methodArgs.length + (isStaticCall ? 0 : 1), isStaticCall);

      String prefix = "";
      String postfix = "";

      if (processor.doesInvokeNeedFrame(methodTranslator.getTranslatorContext(), methodName,
          methodArgs, methodResult)) {
        prefix = generateFramePrefix(argAreaSize, totalFrameSize);
        postfix = generateFramePostfix(argAreaSize, totalFrameSize);
      }

      out.write(prefix);
      out.write(NEXT_LINE);
      for (final String s : processor.generateInvocation(methodTranslator.getTranslatorContext(),
          methodName, methodArgs, methodResult)) {
        out.write(s);
        if (!s.endsWith("\n")) {
          out.write(NEXT_LINE);
        }
      }
      out.write(NEXT_LINE);
      out.write(postfix);
      out.write(NEXT_LINE);

      methodTranslator.getTranslatorContext().registerCalledBootClassProcesser(processor);

      result = true;
    }

    return result;
  }

  /**
   * Get the target method for an invoke instruction.
   *
   * @param methodTranslator a method translator, must not be null
   * @param instruction      an invoking instruction, must not be null
   * @return a MethodGen object which is the target for the instruction or null if the target method is unknown for the translator
   * @see MethodTranslator
   * @see MethodGen
   */
  public MethodGen getInvokedMethod(final MethodTranslator methodTranslator,
                                    final InvokeInstruction instruction) {
    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    final ObjectType objType = this.getObjectType(methodTranslator, instruction);

    String className = objType.getClassName();
    final String methodName = instruction.getMethodName(constantPool);

    return methodTranslator.getTranslatorContext()
        .getMethodContext().findMethod(
            new MethodID(className, methodName,
                instruction.getReturnType(constantPool),
                instruction.getArgumentTypes(constantPool)));
  }

  /**
   * Get the object type for
   *
   * @param methodTranslator a method translator, must not be null
   * @param instruction      an invoke instruction, must not be null
   * @return the object type for the invoking instruction
   */
  public ObjectType getObjectType(final MethodTranslator methodTranslator,
                                  final InvokeInstruction instruction) {
    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    return (ObjectType) instruction.getReferenceType(constantPool);
  }

  /**
   * Get the label name for the method is invoked by an invoke instruction.
   *
   * @param methodTranslator a method translator, must not be null
   * @param instruction      an invoke instruction, must not be null
   * @return the label name for the invoking method
   */
  public String getMethodLabel(final MethodTranslator methodTranslator,
                               final InvokeInstruction instruction) {
    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    final ObjectType objType = getObjectType(methodTranslator, instruction);
    return makeLabelNameForMethod(objType.getClassName(),
        instruction.getMethodName(constantPool), instruction.getReturnType(constantPool),
        instruction.getArgumentTypes(constantPool));
  }

  /**
   * Check that a method object is not null.
   *
   * @param method           the method object to be checked
   * @param methodTranslator a method translator, must not be null
   * @param instruction      an invoke instruction, must not be null
   */
  public void assertMethodIsNotNull(final MethodGen method, final MethodTranslator methodTranslator,
                                    final InvokeInstruction instruction) {
    if (method == null) {
      final String className = getObjectType(methodTranslator, instruction).getClassName();
      final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
      final String message =
          "Can't find method " + className + '.' + instruction.getMethodName(constantPool) + " " +
              instruction.getSignature(constantPool);
      methodTranslator.getTranslatorContext().getLogger().logError(message);
      throw new NullPointerException(message);
    }
  }
}
