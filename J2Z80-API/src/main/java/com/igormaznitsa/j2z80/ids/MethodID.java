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

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.LabelUtils;
import java.util.Arrays;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class MethodID {
    private final String methodId;
    private final String methodLabel;
    private final String className;
    private final String methodName;
    private final Type returnType;
    private final Type [] argTypes;
    private final ClassID classId;    
    
    public MethodID(final MethodGen m){
        this(m.getClassName(),m.getName(),m.getReturnType(),m.getArgumentTypes());
    }
    
    public MethodID(final ClassGen c, final Method m) {
        this(c.getClassName(),m);
    }
    
    public MethodID(final String c, final Method m) {
        this(c,m.getName(),m.getReturnType(),m.getArgumentTypes());
    }

    public MethodID(final String className, final String methodName, final Type returnType, final Type [] argTypes){
        Assert.assertNotNull("Arguments must not contain null", className, methodName, returnType, argTypes);
        this.methodId = className+'.'+methodName+'.'+Type.getMethodSignature(returnType, argTypes);
        this.methodLabel = LabelUtils.makeLabelNameForMethod(className, methodName, returnType, argTypes);
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.argTypes = argTypes;
        classId = new ClassID(className);
    }
    
    public ClassID getClassID() {
        return classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Type getReturnType(){
        return returnType;
    }
    
    public Type [] getArgs(){
        return argTypes; 
    }
    
    @Override
    public int hashCode(){
        return methodId.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof MethodID) {
            final MethodID meth = (MethodID) obj;
            return methodId.equals(meth.methodId);
        }
        return false;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    @Override
    public String toString(){
      return methodId+"("+methodLabel+")";  
    }

    public Method findCompatibleMethod(final ClassGen cgen) {
        Assert.assertNotNull("Class must not be null", cgen);
        for(final Method m : cgen.getMethods()){
            if (methodName.equals(m.getName()) && Arrays.deepEquals(argTypes, m.getArgumentTypes()) && returnType.equals(m.getReturnType())){
                return m;
            }
        }
        return null;
    }
}
