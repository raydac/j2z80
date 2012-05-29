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
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * Auxiliary methods to generate different labels, also it contains methods to work with stack frames
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public enum LabelAndFrameUtils {

    ;
        
    /**
     * Generate a label name based on a java class instance
     * @param javaClass a java class to be used to generate the label name, must not be null
     * @return the generated string is the valid label for the java class
     * @see org.apache.bcel.classfile.JavaClass
     */
    public static String makeLabelNameForClass(final JavaClass javaClass) {
        return makeLabelNameForClass(javaClass.getClassName());
    }

    /**
     * Generate a label name based on a string class name canonical representation
     * @param javaClassName a class name to be used as the source for the operation, must not be null
     * @return the generated string is the label for the class name
     */
    public static String makeLabelNameForClass(final String javaClassName) {
        return normalizeString(javaClassName);
    }

    /**
     * Generate a label name for a class method info object
     * @param methodInfo a class method info object to be used for the operation, must not be null
     * @return the generated string is the label for the class method info object
     * @see ClassMethodInfo
     */
    public static String makeLabelNameForMethod(final ClassMethodInfo methodInfo) {
        return makeLabelNameForMethod(methodInfo.getClassInfo().getClassName(), methodInfo.getMethodInfo().getName(), methodInfo.getMethodInfo().getReturnType(), methodInfo.getMethodInfo().getArgumentTypes());
    }

    /**
     * Generate a label name for a method
     * @param methodGen a method object to be used for the operation, must not be null
     * @return the generated string is the label for the method object
     * @see org.apache.bcel.generic.MethodGen
     */
    public static String makeLabelNameForMethod(final MethodGen methodGen) {
        return makeLabelNameForMethod(methodGen.getClassName(), methodGen.getName(), methodGen.getReturnType(), methodGen.getArgumentTypes());
    }

    /**
     * Generate a label for a method
     * @param className the name of the class that is the owner of the method, must not be null
     * @param methodName the method name, must not be null
     * @param resultType the signature of the result type for the method, must not be null
     * @param argumentTypes an array contains the signatures for all arguments of the method, must not be null or contain null
     * @return the generated string is the label for the method
     * @see org.apache.bcel.generic.Type
     */
    public static String makeLabelNameForMethod(final String className, final String methodName, final Type resultType, final Type[] argumentTypes) {
        return makeLabelNameForClass(className) + '.' + methodName + '#' + normalizeString(Type.getMethodSignature(resultType, argumentTypes));
    }

    /**
     * Generate a label name for a field of a java class
     * @param javaClass the java class contains the field, must not be null
     * @param field the field object which will be used in the operation, must not be null
     * @return the generated string is the label for the class field
     */
    public static String makeLabelNameForField(final JavaClass javaClass, final FieldGen field) {
        return makeLabelNameForField(javaClass.getClassName(), field.getName(), field.getType());
    }

    /**
     * Generate a label name for a class field
     * @param javaClassName the java class canonical name, must not be null
     * @param fieldName the field name, must not be null
     * @param fieldType the signature of the field type, must not be null
     * @return the generated string is the label of the class field
     * @see org.apache.bcel.generic.Type
     */
    public static String makeLabelNameForField(final String javaClassName, final String fieldName, final Type fieldType) {
        return makeLabelNameForClass(javaClassName) + '.' + fieldName + '#' + normalizeString(fieldType.getSignature());
    }

    /**
     * Generate a label name for a constant pool item
     * @param classGen a class object that is the owner of the class pool, must not be null
     * @param index the class pool item index
     * @return the generated string is the label for the class pool item
     * @see org.apache.bcel.generic.ClassGen
     */
    public static String makeLabelForConstantPoolItem(final ClassGen classGen, final int index) {
        return makeLabelForConstantPoolItem(classGen.getJavaClass(), index);
    }

    /**
     * Generate a label name for a constant pool item
     * @param javaClass a class object that is the owner of the class pool, must not be null
     * @param index the class pool item index
     * @return the generated string is the label for the class pool item
     * @see org.apache.bcel.classfile.JavaClass
     */
    public static String makeLabelForConstantPoolItem(final JavaClass javaClass, final int index) {
        return (makeLabelNameForClass(javaClass) + "[" + index + ']');
    }

    /**
     * An auxiliary method to process string and change all incompatible chars to allowed variants
     * @param str the string to be processed, must not be null
     * @return the generated string is the allowed variant of the processed string
     */
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

    /**
     * Generate the label for a field offset data, it shows the offset to the field in a memory block which contain the class data
     * @param className the name of the class that is owner for the field, must not be null
     * @param fieldName the field name, must not be null
     * @param fieldType the field type signature, must not be null
     * @return the generated string is the label for the memory area containing the offset data to the field
     */
    public static String makeLabelNameForFieldOffset(final String className, final String fieldName, final Type fieldType) {
        return makeLabelNameForClass(className) + '.' + fieldName + '#' + normalizeString(fieldType.getSignature()) + "_OFFSET";
    }

    /**
     * Extract the class name from the whole class path
     * @param classPath the whole class path
     * @return the extracted class name
     */
    public static String extractClassName(final String classPath) {
        int index = classPath.lastIndexOf('/');
        if (index < 0) {
            index = classPath.lastIndexOf('.');
        }

        if (index >= 0) {
            return classPath.substring(index + 1);
        }
        return classPath;
    }

    /**
     * Generate a label for a jump position inside a method
     * @param methodInfo the class method info, must not be null
     * @param instructionPosition the destination instruction position inside the method code block
     * @return the generated string is the label for the destination instruction inside the method code
     */
    public static String makeClassMethodJumpLabel(final ClassMethodInfo methodInfo, final int instructionPosition) {
        return makeClassMethodJumpLabel(methodInfo.getClassInfo(), methodInfo.getMethodGen(), instructionPosition);
    }

    /**
     * Generate a label for a jump position inside a method
     * @param classGen the class object contains the method, must not be null
     * @param method the method object contains the instruction, must not be null
     * @param instructionPosition  the destination instruction position inside the method code block
     * @return the generated string is the label for the destination instruction inside the method code
     */
    public static String makeClassMethodJumpLabel(final ClassGen classGen, final MethodGen method, final int instructionPosition) {
        final String methodLabel = makeLabelNameForMethod(method);
        return methodLabel + "." + Integer.toHexString(instructionPosition).toUpperCase();
    }

    /**
     * Generate a label for a class size info
     * @param className the class name, must not be null
     * @return the generated string is the label for the class size information
     */
    public static String makeLabelForClassSizeInfo(final String className) {
        return "___sizeof<" + className + ">____";
    }

    /**
     * Generate a label for a class size info
     * @param type  the type information
     * @return the generated string is the label for the class size information
     */
    public static String makeLabelForClassSizeInfo(final ObjectType type) {
        return makeLabelForClassSizeInfo(type.getClassName());
    }

    /**
     * Generate a class id label
     * @param classID the class id object, must not be null
     * @return the generated string is the label for the class identifier constant
     */
    public static String makeLabelForClassID(final ClassID classID) {
        return classID.makeClassLabel() + "_CLASS_ID";
    }

    /**
     * Generate a method id label
     * @param methodID the method id object, must not be null
     * @return the generated string is the label for the method identifier constant
     */
    public static String makeLabelForMethodID(final MethodID methodID) {
        return methodID.getMethodLabel() + "_METH_ID";
    }

    /**
     * Generate a label for a virtual method table record
     * @param className the class name, must not be null
     * @param methodName the method name, must ot be null
     * @param returnType the return type signature, must not be null
     * @param argType the argument type signatures, must not be null
     * @return the generated string is the label for a virtual method table record
     */
    public static String makeLabelForVirtualMethodRecord(final String className, final String methodName, final Type returnType, final Type[] argType) {
        return makeLabelNameForMethod(className, methodName, returnType, argType) + "_VT_REC";
    }

    /**
     * Calculate the stack frame size for a method
     * @param argNumber the number of method arguments
     * @param maxLocals the maximum number of local variables
     * @param isStatic the flag shows that the method is a static one if the flag is true
     * @return the stack frame size needed by the method in bytes
     */
    public static int calculateFrameSizeForMethod(final int argNumber, final int maxLocals, final boolean isStatic) {
        return (Math.max(argNumber, maxLocals) + (isStatic ? 0 : 1)) << 1;
    }
    
    /**
     * Generate the label for embedded binary resource
     * @param path the resource path, must not be null
     * @return the generated string is the label for the resource area
     */
    public static String makeLabelForBinaryResource(final String path){
        return "BINRSRC_"+normalizeString(path);
    }
}
