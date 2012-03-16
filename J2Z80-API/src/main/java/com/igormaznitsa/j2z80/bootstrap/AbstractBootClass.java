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
package com.igormaznitsa.j2z80.bootstrap;

import com.igormaznitsa.j2z80.TranslatorContext;
import java.util.*;
import org.apache.bcel.generic.Type;

public abstract class AbstractBootClass {
    private static final String [] EMPTY_STRING_ARRAY = new String [0];
    
    private static final Map<String,AbstractBootClass> insideCache = new HashMap<String,AbstractBootClass>();
    
    public static AbstractBootClass findProcessor(final String className){
        AbstractBootClass result = insideCache.get(className);
        if (result == null){
            final String newname = AbstractBootClass.class.getPackage().getName()+"."+className;
            try {
                final Class<? extends AbstractBootClass> japiClass = Class.forName(newname).asSubclass(AbstractBootClass.class);
                result = (AbstractBootClass)japiClass.newInstance();
                insideCache.put(className, result);
            }catch(ClassNotFoundException ex){
            }catch(IllegalAccessException ex){
                throw new RuntimeException("Can't get access to a bootstrap class ["+className+']',ex);
            }catch(InstantiationException ex){
                throw new RuntimeException("Can't instantiate a bootstrap class ["+className+']',ex);
            }
        }
        return result;
    }

    protected String extractEmulatedJavaClassName(){
        return this.getClass().getCanonicalName().substring(AbstractBootClass.class.getPackage().getName().length()+1);
    }
    
    public void throwBootClassExceptionForMethod(final String methodName, final Type result, final Type [] args){
        final String className = extractEmulatedJavaClassName();
        final String methodSignature = Type.getMethodSignature(result, args);
        throw new BootClassException("Unsupported method ["+className+'.'+methodName+" "+methodSignature+"]", className, methodName, methodSignature);
    }
    
    public void throwBootClassExceptionForField(final String fieldName, final Type type){
        final String className = extractEmulatedJavaClassName();
        throw new BootClassException("Unsupported field ["+className+'.'+fieldName+" "+type.toString()+"]", className, fieldName, type.getSignature());
    }
    
    public abstract boolean doesInvokeNeedFrame(TranslatorContext translator, String methodName, Type[] methodArguments, Type resultType);
    public abstract String [] generateInvokation(TranslatorContext translator, String methodName, Type [] methodArguments, Type resultType);
    public abstract String [] generateGetField(TranslatorContext translator, String fieldName, Type fieldType, boolean isStatic);
    public abstract String [] generateSetField(TranslatorContext translator, String fieldName, Type methodSignature, boolean isStatic);
    public String [] getAdditionalText(){
        return EMPTY_STRING_ARRAY;
    }
}
