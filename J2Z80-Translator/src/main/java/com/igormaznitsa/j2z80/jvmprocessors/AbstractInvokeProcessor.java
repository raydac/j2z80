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
import com.igormaznitsa.j2z80.aux.LabelUtils;
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

public abstract class AbstractInvokeProcessor extends AbstractJvmCommandProcessor implements NeedsMemoryManager {

    public static int calculateArgumentBlockSize(final int argNumber, final boolean isStatic) {
        int num = argNumber;
        if (!isStatic) {
            num++;
        }
        return num << 1;
    }

    public static int calculateObjectOffsetOnStack(final int argumentBlockSize) {
        return argumentBlockSize - 2;
    }

    public static int calculateArgumentBlockSize(final MethodGen invokingMethod) {
        return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length, invokingMethod.isStatic());
    }

    public static int calculateTotalFrameSize(final MethodGen invokingMethod) {
        return calculateArgumentBlockSize(invokingMethod.getArgumentTypes().length, invokingMethod.isStatic());
    }

    public int calculateTotalFrameSize(final int args, final int maxLocals, final boolean isStatic) {
        int argNumber = args;

        if (!isStatic) {
            argNumber++;
        }

        if (argNumber > maxLocals) {
            throw new IllegalStateException("Frame size is less than arguments number");
        }

        return maxLocals << 1;
    }

    protected boolean checkBootstrapCall(final MethodTranslator methodTranslator, final InvokeInstruction instr, final Writer out) throws IOException {

        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instr);
        final String methodName = instr.getMethodName(constantPool);
        final Type[] methodArgs = instr.getArgumentTypes(constantPool);
        final Type methodResult = instr.getReturnType(constantPool);

        final MethodGen methodGen = methodTranslator.getTranslatorContext().getMethodContext().findMethod(new MethodID(objType.getClassName(), methodName, methodResult, methodArgs));

        if (methodGen != null) {
            return false;
        }

        final AbstractBootClass processor = AbstractBootClass.findProcessor(objType.getClassName());

        boolean result = false;
        
        if (processor != null) {
            final boolean isStaticCall = instr instanceof INVOKESPECIAL;
            final int argAreaSize = calculateArgumentBlockSize(methodArgs.length, isStaticCall);
            final int totalFrameSize = calculateTotalFrameSize(methodArgs.length, methodArgs.length + (isStaticCall ? 0 : 1), isStaticCall);

            String prefix = "";
            String postfix = "";

            if (processor.doesInvokeNeedFrame(methodTranslator.getTranslatorContext(), methodName, methodArgs, methodResult)) {
                prefix = generateFramePrefix(argAreaSize, totalFrameSize);
                postfix = generateFramePostfix(argAreaSize, totalFrameSize);
            }

            out.write(prefix);
            out.write("\n");
            for (final String s : processor.generateInvokation(methodTranslator.getTranslatorContext(), methodName, methodArgs, methodResult)) {
                out.write(s);
                if (!s.endsWith("\n")) {
                    out.write("\n");
                }
            }
            out.write("\n");
            out.write(postfix);
            out.write("\n");

            methodTranslator.getTranslatorContext().registerCalledBootClassProcesser(processor);

            result = true;
        }

        return result;
    }

    public MethodGen getInvokedMethod(final MethodTranslator methodTranslator, final InvokeInstruction instr) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instr);
        return methodTranslator.getTranslatorContext().getMethodContext().findMethod(new MethodID(objType.getClassName(), instr.getMethodName(constantPool), instr.getReturnType(constantPool), instr.getArgumentTypes(constantPool)));
    }

    public ObjectType getObjectType(final MethodTranslator methodTranslator, final InvokeInstruction instr) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        return (ObjectType) instr.getReferenceType(constantPool);
    }

    public String getMethodLabel(final MethodTranslator methodTranslator, final InvokeInstruction instr) {
        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = getObjectType(methodTranslator, instr);
        return LabelUtils.makeLabelNameForMethod(objType.getClassName(), instr.getMethodName(constantPool), instr.getReturnType(constantPool), instr.getArgumentTypes(constantPool));
    }

    public static String generateFramePrefix(final int argMemorySize, final int frameMemSize) {
        return "LD A," + argMemorySize + "\n"
                + "LD BC," + frameMemSize + "\n"
                + "CALL " + SUB_BEFORE_INVOKE + "\n";
    }

    public static String generateFramePostfix(final int argMemorySize, final int frameMemSize) {
        return "CALL " + SUB_AFTER_INVOKE + "\n";
    }

    public void assertMethodIsNotNull(final MethodGen method, final MethodTranslator mTranslator, final InvokeInstruction inst) {
        if (method == null) {
            final String className = getObjectType(mTranslator, inst).getClassName();
            final ConstantPoolGen constantPool = mTranslator.getConstantPool();
            final String str = "Can't find method " + className + '.' + inst.getMethodName(constantPool) + " " + inst.getSignature(constantPool);
            mTranslator.getTranslatorContext().getLogger().logError(str);
            throw new NullPointerException(str);
        }
    }
}
