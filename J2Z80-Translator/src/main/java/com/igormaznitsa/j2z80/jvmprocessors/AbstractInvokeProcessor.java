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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
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
public abstract class AbstractInvokeProcessor extends AbstractJvmCommandProcessor implements NeedsMemoryManager {

    /**
     * Calculate the size of memory block in bytes to keep arguments for a method.
     * @param argNumber the number of arguments needed by the method
     * @param isStatic the flag shows that the method is a static one if it is true
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
     * @param invokingMethod a method to be used for calculation, must not be null
     * @return the memory block size in bytes
     */
    public static int calculateArgumentBlockSize(final MethodGen invokingMethod) {
        return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length, invokingMethod.isStatic());
    }

    /**
     * Calculate the offset to an object reference on the stack.
     * @param argumentBlockSize  the argument block size in bytes
     * @return the offset in the block to the object reference
     */
    public static int calculateObjectOffsetOnStack(final int argumentBlockSize) {
        return argumentBlockSize - 2;
    }

    /**
     * Calculate whole stack frame size in bytes for a method ignoring local variables.
     * @param invokingMethod a method to be used for calculations, must not be null
     * @return the whole memory frame size in bytes
     */
    public static int calculateTotalFrameSizeWithoutLocals(final MethodGen invokingMethod) {
        return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length, invokingMethod.isStatic());
    }

    /**
     * Calculate whole stack frame size in bytes based on max local variable number data.
     * @param args the number of arguments for a method
     * @param maxLocals the number of local variables for a method
     * @param isStatic the flag shows that the method is a static one if it is true
     * @return the memory frame size in bytes
     */
    public int calculateTotalFrameSizeWithLocals(final int args, final int maxLocals, final boolean isStatic) {
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
     * @param methodTranslator a method translator called the method, must not be null
     * @param instruction an invoke instruction to be checked, must not be null  
     * @param out the output stream to write commands
     * @return true if the instruction invokes a bootstrap class and it has been processed by the method, else false
     * @throws IOException it will be thrown if any transport problem in the method
     */
    protected boolean checkBootstrapCall(final MethodTranslator methodTranslator, final InvokeInstruction instruction, final Writer out) throws IOException {

        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instruction);
        final String methodName = instruction.getMethodName(constantPool);
        final Type[] methodArgs = instruction.getArgumentTypes(constantPool);
        final Type methodResult = instruction.getReturnType(constantPool);

        final MethodGen methodGen = methodTranslator.getTranslatorContext().getMethodContext().findMethod(new MethodID(objType.getClassName(), methodName, methodResult, methodArgs));

        if (methodGen != null) {
            return false;
        }

        final AbstractBootClass processor = AbstractBootClass.findProcessor(objType.getClassName());

        boolean result = false;
        
        if (processor != null) {
            final boolean isStaticCall = instruction instanceof INVOKESPECIAL;
            final int argAreaSize = calculateArgumentBlockSize(methodArgs.length, isStaticCall);
            final int totalFrameSize = calculateTotalFrameSizeWithLocals(methodArgs.length, methodArgs.length + (isStaticCall ? 0 : 1), isStaticCall);

            String prefix = "";
            String postfix = "";

            if (processor.doesInvokeNeedFrame(methodTranslator.getTranslatorContext(), methodName, methodArgs, methodResult)) {
                prefix = generateFramePrefix(argAreaSize, totalFrameSize);
                postfix = generateFramePostfix(argAreaSize, totalFrameSize);
            }

            out.write(prefix);
            out.write(NEXT_LINE);
            for (final String s : processor.generateInvocation(methodTranslator.getTranslatorContext(), methodName, methodArgs, methodResult)) {
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
     * @param methodTranslator a method translator, must not be null
     * @param instruction an invoking instruction, must not be null
     * @return a MethodGen object which is the target for the instruction or null if the target method is unknown for the translator
     * @see MethodTranslator
     * @see MethodGen
     */
    public MethodGen getInvokedMethod(final MethodTranslator methodTranslator, final InvokeInstruction instruction) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instruction);
        return methodTranslator.getTranslatorContext().getMethodContext().findMethod(new MethodID(objType.getClassName(), instruction.getMethodName(constantPool), instruction.getReturnType(constantPool), instruction.getArgumentTypes(constantPool)));
    }

    /**
     * Get the object type for 
     * @param methodTranslator a method translator, must not be null
     * @param instruction an invoke instruction, must not be null
     * @return the object type for the invoking instruction
     */
    public ObjectType getObjectType(final MethodTranslator methodTranslator, final InvokeInstruction instruction) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        return (ObjectType) instruction.getReferenceType(constantPool);
    }

    /**
     * Get the label name for the method is invoked by an invoke instruction.
     * @param methodTranslator a method translator, must not be null
     * @param instruction an invoke instruction, must not be null 
     * @return the label name for the invoking method
     */
    public String getMethodLabel(final MethodTranslator methodTranslator, final InvokeInstruction instruction) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instruction);
        return LabelAndFrameUtils.makeLabelNameForMethod(objType.getClassName(), instruction.getMethodName(constantPool), instruction.getReturnType(constantPool), instruction.getArgumentTypes(constantPool));
    }

    /**
     * Generate the prefix for a method invocation
     * @param argMemorySize the memory block size for arguments of the method
     * @param frameMemSize the stack frame needed by the method
     * @return the string containing the prefix code for the method invocation
     */
    public static String generateFramePrefix(final int argMemorySize, final int frameMemSize) {
        return "LD A," + argMemorySize + NEXT_LINE
                + "LD BC," + frameMemSize + NEXT_LINE
                + "CALL " + SUB_BEFORE_INVOKE + NEXT_LINE;
    }

    /**
     * Generate the postfix for a method invocation
     * @param argMemorySize the memory block size for arguments of the method
     * @param frameMemSize the stack frame needed by the method
     * @return the string containing the postfix code for the method invocation
     */
    public static String generateFramePostfix(final int argMemorySize, final int frameMemSize) {
        return "CALL " + SUB_AFTER_INVOKE + NEXT_LINE;
    }

    /**
     * Check that a method object is not null.
     * @param method the method object to be checked
     * @param methodTranslator a method translator, must not be null
     * @param instruction an invoke instruction, must not be null
     */
    public void assertMethodIsNotNull(final MethodGen method, final MethodTranslator methodTranslator, final InvokeInstruction instruction) {
        if (method == null) {
            final String className = getObjectType(methodTranslator, instruction).getClassName();
            final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
            final String message = "Can't find method " + className + '.' + instruction.getMethodName(constantPool) + " " + instruction.getSignature(constantPool);
            methodTranslator.getTranslatorContext().getLogger().logError(message);
            throw new NullPointerException(message);
        }
    }
}
