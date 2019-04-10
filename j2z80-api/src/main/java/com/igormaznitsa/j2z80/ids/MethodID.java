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

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.util.Arrays;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 * The Class describes an identifier for a method
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MethodID {
    private final String methodId;
    private final String methodLabel;
    private final String className;
    private final String methodName;
    private final Type returnType;
    private final Type [] argTypes;
    private final ClassID classId;    
    
    /**
     * A Constructor
     * @param m a MethodGen object, must not be null 
     */
    public MethodID(final MethodGen m){
        this(m.getClassName(),m.getName(),m.getReturnType(),m.getArgumentTypes());
    }
    
    /**
     * A Constructor
     * @param c the ClassGen owns the method, must not be null
     * @param m  the method, must not be null
     */
    public MethodID(final ClassGen c, final Method m) {
        this(c.getClassName(),m);
    }
    
    /**
     * A Constructor
     * @param className the class name, must not be null
     * @param method the method, must not be null
     */
    public MethodID(final String className, final Method method) {
        this(className,method.getName(),method.getReturnType(),method.getArgumentTypes());
    }

    /**
     * A Constructor
     * @param className the class name, must not be null
     * @param methodName the method name, must not be null
     * @param returnType the return type signature for the method, must not be null
     * @param argTypes  the argument type signatures for the method, must not be null
     */
    public MethodID(final String className, final String methodName, final Type returnType, final Type [] argTypes){
        Assert.assertNotNull("Arguments must not contain null", className, methodName, returnType, argTypes);
        this.methodId = className+'.'+methodName+'.'+Type.getMethodSignature(returnType, argTypes);
        this.methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(className, methodName, returnType, argTypes);
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.argTypes = argTypes;
        classId = new ClassID(className);
    }
    
    /**
     * Get the class Id for the class owns the method
     * @return the class id object
     */
    public ClassID getClassID() {
        return classId;
    }
    
    /**
     * Get the class name of the class owns the menthod
     * @return the class name
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Get the method name
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }
    
    /**
     * Get the return type signature for the method
     * @return the return type
     */
    public Type getReturnType(){
        return returnType;
    }
    
    /**
     * Get the argument type signatures for the method
     * @return the argument types for the method
     */
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

    /**
     * Get the method label
     * @return the method label as String
     */
    public String getMethodLabel() {
        return methodLabel;
    }

    @Override
    public String toString(){
      return methodId+"("+methodLabel+")";  
    }

    /**
     * Find compatible method inside a class
     * @param cgen a class object where the compatible method will be looked for
     * @return a found compatible method if it is found or null if not found
     */
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
