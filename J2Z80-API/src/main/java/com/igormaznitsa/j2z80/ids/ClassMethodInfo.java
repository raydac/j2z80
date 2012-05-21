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

public class ClassMethodInfo {

    private final ClassGen classInfo;
    private final Method methodInfo;
    private MethodGen methodGen;
    private final int id;

    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final int id) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
        this.id = id;
    }

    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo) {
        this(classInfo, methodInfo, -1);
    }

    public ClassMethodInfo(final ClassGen classInfo, final Method methodInfo, final MethodGen methodGen) {
        this(classInfo, methodInfo, -1);
        this.methodGen = methodGen;
    }

    public int getUID() {
        return id;
    }

    public ClassGen getClassInfo() {
        return this.classInfo;
    }

    public Method getMethodInfo() {
        return this.methodInfo;
    }

    public String getPackageName() {
        final String fullClassName = this.classInfo.getClassName();
        final int index = fullClassName.lastIndexOf('.');
        if (index < 0) {
            return "";
        } else {
            return fullClassName.substring(0,index);
        }
    }

    public String getOnlyClassName() {
        final String fullClassName = this.classInfo.getClassName();
        final int index = fullClassName.lastIndexOf('.');
        if (index < 0) {
            return fullClassName;
        } else {
            return fullClassName.substring(index + 1);
        }
    }

    public String getMethodName() {
        return this.methodInfo == null ?  null : this.methodInfo.getName();
    }

    public String getMethodSignature() {
        return this.methodInfo == null ? null : this.methodInfo.getSignature();
    }

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
    
    public boolean isNative() {
        return methodInfo == null ? false : methodInfo.isNative();
    }
}
