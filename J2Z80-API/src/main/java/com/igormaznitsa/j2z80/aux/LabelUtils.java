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
package com.igormaznitsa.j2z80.aux;

import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import java.util.jar.JarEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public enum LabelUtils {

    ;
        
    public static String makeLabelNameForClass(final JavaClass javaClass) {
        return makeLabelNameForClass(javaClass.getClassName());
    }

    public static String makeLabelNameForClass(final String javaClassName) {
        return normalizeString(javaClassName);
    }

    public static String makeLabelNameForMethod(final ClassMethodInfo methodInfo) {
        return makeLabelNameForMethod(methodInfo.getClassInfo().getClassName(), methodInfo.getMethodInfo().getName(), methodInfo.getMethodInfo().getReturnType(), methodInfo.getMethodInfo().getArgumentTypes());
    }

    public static String makeLabelNameForMethod(final MethodGen methodGen) {
        return makeLabelNameForMethod(methodGen.getClassName(), methodGen.getName(), methodGen.getReturnType(), methodGen.getArgumentTypes());
    }

    public static String makeLabelNameForMethod(final String className, final String method, final Type result, final Type[] arguments) {
        return makeLabelNameForClass(className) + '.' + method + '#' + normalizeString(Type.getMethodSignature(result, arguments));
    }

    public static String makeLabelNameForField(final JavaClass javaClass, final FieldGen field) {
        return makeLabelNameForField(javaClass.getClassName(), field.getName(), field.getType());
    }

    public static String makeLabelNameForField(final String javaClass, final String fieldName, final Type fieldType) {
        return makeLabelNameForClass(javaClass) + '.' + fieldName + '#' + normalizeString(fieldType.getSignature());
    }

    public static String makeLabelForConstantPoolItem(final ClassGen cg, final int index) {
        return makeLabelForConstantPoolItem(cg.getJavaClass(), index);
    }

    public static String makeLabelForConstantPoolItem(final JavaClass javaClass, final int index) {
        return (makeLabelNameForClass(javaClass) + "[" + index + ']');
    }

    public static String normalizeString(final String str) {
        final StringBuilder result = new StringBuilder(str.length());
        for (final char chr : str.toCharArray()) {
            switch (chr) {
                case '/':
                    result.append('.');
                    break;
                case '(':
                    result.append('[');
                    break;
                case ')':
                    result.append(']');
                    break;
                case '+':
                    result.append("x");
                    break;
                case ':':
                    result.append("|");
                    break;
                case ';':
                    result.append("j");
                    break;
                case '-':
                    result.append('_');
                    break;
                case '$':
                    result.append('S');
                    break;
                default:
                    result.append(chr);
                    break;
            }
        }

        return result.toString();
    }

    public static String makeLabelNameForFieldOffset(final String javaClass, final String fieldName, final Type fieldType) {
        return makeLabelNameForClass(javaClass) + '.' + fieldName + '#' + normalizeString(fieldType.getSignature()) + "_OFFSET";
    }

    public static String replaceJarEntryName(final JarEntry entry, final String newName) {
        final String entryName = entry.getName();
        final String str = entryName.replace('\\', '/');
        final int lastIndex = str.lastIndexOf('/');

        String result = newName;

        if (lastIndex >= 0) {
            result = entryName.substring(0, lastIndex + 1) + newName;
        }

        return result;
    }

    public static String extractClassName(final String className) {
        int index = className.lastIndexOf('/');
        if (index < 0) {
            index = className.lastIndexOf('.');
        }

        if (index >= 0) {
            return className.substring(index + 1);
        }
        return className;
    }

    public static String makeClassMethodJumpLabel(final ClassMethodInfo methodInfo, final int instructionPosition) {
        return makeClassMethodJumpLabel(methodInfo.getClassInfo(), methodInfo.getMethodGen(), instructionPosition);
    }

    public static String makeClassMethodJumpLabel(final ClassGen classGen, final MethodGen method, final int instructionPosition) {
        final String methodLabel = makeLabelNameForMethod(method);
        return methodLabel + "." + Integer.toHexString(instructionPosition).toUpperCase();
    }

    public static String makeLabelForClassSizeInfo(final String className) {
        return "___sizeof<" + className + ">____";
    }

    public static String makeLabelForClassSizeInfo(final ObjectType type) {
        return makeLabelForClassSizeInfo(type.getClassName());
    }

    public static String makeLabelForClassID(final ClassID classID) {
        return classID.makeClassLabel() + "_CLASS_ID";
    }

    public static String makeLabelForMethodID(final MethodID methodID) {
        return methodID.getMethodLabel() + "_METH_ID";
    }

    public static String makeLabelForVirtualMethodRecord(final String className, final String methodName, final Type returnType, final Type[] argType) {
        return makeLabelNameForMethod(className, methodName, returnType, argType) + "_VT_REC";
    }

    public static int calculateFrameSizeForMethod(final int argNumber, final int maxLocals, final boolean isStatic) {
        return (Math.max(argNumber, maxLocals) + (isStatic ? 0 : 1)) << 1;
    }
    
    public static String makeLabelForBinaryResource(final String path){
        return "BINRSRC_"+normalizeString(path);
    }
}
