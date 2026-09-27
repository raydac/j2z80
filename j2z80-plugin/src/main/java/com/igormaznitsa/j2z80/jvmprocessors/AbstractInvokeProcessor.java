/*
 * Copyright 2012-2026 Igor Maznitsa.
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

import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.countArgumentSlots;
import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelNameForMethod;

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.translator.CheckedExceptionSupport;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InstructionHandle;
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
    return calculateArgumentBlockSize(countArgumentSlots(invokingMethod.getArgumentTypes()),
        invokingMethod.isStatic());
  }

  public static String pushReturnedValue(final Type returnType) {
    if (returnType.getType() == Type.VOID.getType()) {
      return "";
    }
    if (returnType.getSize() == 2) {
      return "PUSH DE" + NEXT_LINE + "PUSH BC" + NEXT_LINE;
    }
    return "PUSH BC" + NEXT_LINE;
  }

  public static String pushReturnedValueAndCheckException(
      final MethodTranslator methodTranslator,
      final InstructionHandle handle,
      final MethodGen invokedMethod
  ) {
    return pushReturnedValue(invokedMethod.getReturnType())
        + CheckedExceptionSupport.afterInvokeCheck(methodTranslator, handle, invokedMethod);
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
   * Calculate whole stack frame size in bytes for a method, including local variables.
   *
   * @param invokingMethod a method to be used for calculations, must not be null
   * @return the whole memory frame size in bytes
   */
  public static int calculateTotalFrameSizeWithLocals(final MethodGen invokingMethod) {
    return calculateTotalFrameSizeWithLocals(
        countArgumentSlots(invokingMethod.getArgumentTypes()),
        invokingMethod.getMaxLocals(),
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
   * A long or double is pushed high word then low word, so the low word is on top.
   * The callee frame addresses slot 0 at the first pushed word, so each pair is
   * exchanged and the low word lands in the first slot.
   */
  public static String orientWideArguments(final Type[] argumentTypes) {
    final StringBuilder oriented = new StringBuilder();
    int offset = 0;
    for (int index = argumentTypes.length - 1; index >= 0; index--) {
      final Type argumentType = argumentTypes[index];
      if (argumentType.getSize() == 2) {
        oriented.append(swapStackPair(offset));
      }
      offset += argumentType.getSize() << 1;
    }
    return oriented.toString();
  }

  private static String swapStackPair(final int offset) {
    return "LD HL," + offset + NEXT_LINE
        + "ADD HL,SP" + NEXT_LINE
        + "LD E,(HL)" + NEXT_LINE
        + "INC HL" + NEXT_LINE
        + "LD D,(HL)" + NEXT_LINE
        + "INC HL" + NEXT_LINE
        + "LD C,(HL)" + NEXT_LINE
        + "INC HL" + NEXT_LINE
        + "LD B,(HL)" + NEXT_LINE
        + "LD (HL),D" + NEXT_LINE
        + "DEC HL" + NEXT_LINE
        + "LD (HL),E" + NEXT_LINE
        + "DEC HL" + NEXT_LINE
        + "LD (HL),B" + NEXT_LINE
        + "DEC HL" + NEXT_LINE
        + "LD (HL),C" + NEXT_LINE;
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
   * {@code maxLocals} already includes {@code this} for instance methods when present in the
   * Code attribute; native methods often report {@code 0}, so argument slots are used as a floor.
   *
   * @param args      the number of arguments for a method (without {@code this})
   * @param maxLocals the maximum number of local variables for a method
   * @param isStatic  the flag shows that the method is a static one if it is true
   * @return the memory frame size in bytes
   */
  public static int calculateTotalFrameSizeWithLocals(final int args, final int maxLocals,
                                                      final boolean isStatic) {
    final int argSlots = args + (isStatic ? 0 : 1);
    return Math.max(argSlots, maxLocals) << 1;
  }

  /**
   * Check the invoke instruction for a bootstrap class and if the invoked class is a bootstrap one then the method will process it by a special way.
   *
   * @param methodTranslator a method translator called the method, must not be null
   * @param instruction      an invoke instruction to be checked, must not be null
   * @param bootstrapClassLoader bootstrap class loader, must not be null
   * @param out              the output stream to write commands
   * @return true if the instruction invokes a bootstrap class, and it has been processed by the method, else false
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
      final boolean isStaticCall = instruction instanceof INVOKESTATIC;
      final int argumentSlots = countArgumentSlots(methodArgs);
      final int argAreaSize = calculateArgumentBlockSize(argumentSlots, isStaticCall);
      final int totalFrameSize = calculateTotalFrameSizeWithLocals(argumentSlots,
          argumentSlots + (isStaticCall ? 0 : 1), isStaticCall);

      String prefix = "";
      String postfix = "";

      if (processor.doesInvokeNeedFrame(methodTranslator.getTranslatorContext(), methodName,
          methodArgs, methodResult)) {
        prefix = orientWideArguments(methodArgs) + generateFramePrefix(argAreaSize, totalFrameSize);
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
