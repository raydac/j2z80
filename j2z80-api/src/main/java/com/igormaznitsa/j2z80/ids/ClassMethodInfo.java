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
package com.igormaznitsa.j2z80.ids;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The Class describes a class method for inside translating operations.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ClassMethodInfo {
    
    private final ClassGen classInfo;
    private final Method methodInfo;
    private MethodGen methodGen;
    private final int id;

    /**
     * A Constructor
     * @param classInfo the ClassGen object contains the method
     * @param methodInfo the Method object
     * @param id the ID of the method object
     */
    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final int id) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
        this.id = id;
    }

    /**
     * A Constructor
     * @param classInfo the ClassGen object contains the method
     * @param methodInfo  the Method object
     */
    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo) {
        this(classInfo, methodInfo, -1);
    }

    /**
     * A Constructor
     * @param classInfo the ClassGen object contains the method
     * @param methodInfo the Method object describes the method
     * @param methodGen the MethodGen object for the method
     */
    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final MethodGen methodGen) {
        this(classInfo, methodInfo, -1);
        this.methodGen = methodGen;
    }

    /**
     * Get UID for the method info
     * @return the UID as integer
     */
    public int getUID() {
        return id;
    }

    /**
     * Get the ClassGen object saved by the info
     * @return the ClassGen object saved bye the info
     */
    public ClassGen getClassInfo() {
        return this.classInfo;
    }

    /**
     * Get the Method object saved by the info
     * @return the Method for the info object
     */
    public Method getMethodInfo() {
        return this.methodInfo;
    }

    /**
     * Get the package name (without the class name) for the class contains the method
     * @return the class package information as String
     */
    public String getPackageName() {
        final String fullClassName = this.classInfo.getClassName();
        final int index = fullClassName.lastIndexOf('.');
        if (index < 0) {
            return "";
        } else {
            return fullClassName.substring(0,index);
        }
    }

    /**
     * Get the canonical class name.
     * @return the canonical class name as String
     */
    public String getCanonicalClassName(){
        return this.classInfo.getClassName();
    }
    
    /**
     * Get only class name (package data excluded) for the class contains the method.
     * @return the class name as String
     */
    public String getOnlyClassName() {
        final String fullClassName = this.classInfo.getClassName();
        final int index = fullClassName.lastIndexOf('.');
        if (index < 0) {
            return fullClassName;
        } else {
            return fullClassName.substring(index + 1);
        }
    }

    /**
     * Get the method name
     * @return the method name as String
     */
    public String getMethodName() {
        return this.methodInfo == null ?  null : this.methodInfo.getName();
    }

    /**
     * Get the method signature
     * @return the method signature as String
     */
    public String getMethodSignature() {
        return this.methodInfo == null ? null : this.methodInfo.getSignature();
    }

    /**
     * Get the MethodGen object linked to the method info
     * @return null if saved method info is null, a MethodGen object if there is MethodGen linked to the info object
     */
    public MethodGen getMethodGen() {
        if (methodInfo == null) {
            return null;
        }
        if (methodGen == null) {
            methodGen = new MethodGen(methodInfo, classInfo.getClassName(), classInfo.getConstantPool());
        }
        return methodGen;
    }

    @Override
    public int hashCode() {
        int hash = classInfo == null ? 0 : classInfo.hashCode();
        return hash ^ (methodInfo == null ? 31 : methodInfo.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ClassMethodInfo) {
            final ClassMethodInfo info = (ClassMethodInfo) obj;
            return classInfo.equals(info.classInfo) && methodInfo.equals(methodInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        if (classInfo!=null){
            result.append(classInfo.getClassName());
        }
        if (methodInfo!=null){
            result.append('#').append(methodInfo.getName()).append(' ').append(methodInfo.getSignature());
        }
        return result.toString();
    }
    
    /**
     * Check that the method is a native one
     * @return returns true if the method is a native one
     */
    public boolean isNative() {
        return methodInfo == null ? false : methodInfo.isNative();
    }
}
