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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.generic.Type;

/**
 * The class is the parent for all bootstrap classes used by the translator, it automates their search and processing.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractBootClass {
    private static final String [] EMPTY_STRING_ARRAY = new String [0];
    
    private static final Map<String,AbstractBootClass> insideCache = new HashMap<String,AbstractBootClass>();
    
    public static AbstractBootClass findProcessor(final String className){
        AbstractBootClass result = insideCache.get(className);
        if (result == null){
            final String newname = AbstractBootClass.class.getPackage().getName()+"."+className;
            try {
                final Class<? extends AbstractBootClass> japiClass = Class.forName(newname).asSubclass(AbstractBootClass.class);
                result = japiClass.getDeclaredConstructor().newInstance();
                insideCache.put(className, result);
            }catch(ClassNotFoundException ex){

            }catch (InvocationTargetException ex){
            }catch (NoSuchMethodException ex){

            }catch(IllegalAccessException ex){
                throw new RuntimeException("Can't get access to a bootstrap class ["+className+']',ex);
            }catch(InstantiationException ex){
                throw new RuntimeException("Can't instantiate a bootstrap class ["+className+']',ex);
            }
        }
        return result;
    }

    /**
     * Extract the emulated class name of the class
     * @return the class name part of the emulated bootstrap class
     */
    protected String extractEmulatedJavaClassName(){
        return this.getClass().getCanonicalName().substring(AbstractBootClass.class.getPackage().getName().length()+1);
    }
    
    /**
     * An Auxiliary method to throw a boot class exception for a method
     * @param methodName the method name, must not be null
     * @param result the method result signature, must not be null
     * @param args the method argument signatures, must not be null
     */
    public void throwBootClassExceptionForMethod(final String methodName, final Type result, final Type [] args){
        final String className = extractEmulatedJavaClassName();
        final String methodSignature = Type.getMethodSignature(result, args);
        throw new BootClassException("Unsupported method ["+className+'.'+methodName+" "+methodSignature+"]", className, methodName, methodSignature);
    }
    
    /**
     * An Auxiliary method to throw a boot class exception for a field
     * @param fieldName the field name, must not be null
     * @param type the field type signature, must not be null
     */
    public void throwBootClassExceptionForField(final String fieldName, final Type type){
        final String className = extractEmulatedJavaClassName();
        throw new BootClassException("Unsupported field ["+className+'.'+fieldName+" "+type.toString()+"]", className, fieldName, type.getSignature());
    }
    
    /**
     * It allows to figure out that a method needs a stack frame when it is invoked
     * @param context the translator context, must not be null
     * @param methodName the method name, must not be null
     * @param methodArguments the method argument signatures, must not be null
     * @param resultType the method result type signature, must not be null
     * @return it returns true if the method needs a stack frame for its work
     */
    public abstract boolean doesInvokeNeedFrame(TranslatorContext context, String methodName, Type[] methodArguments, Type resultType);
    
    /**
     * Generate method invocation code
     * @param context the translator context, must not be null
     * @param methodName the method name, must not be null
     * @param methodArguments the method argument signatures, must not be null
     * @param resultType the method result signature, must not be null
     * @return a string array contains assembler commands to invoke the method
     */
    public abstract String [] generateInvocation(TranslatorContext context, String methodName, Type [] methodArguments, Type resultType);
    
    /**
     * Generate a field getter
     * @param context the translator context, must not be null
     * @param fieldName the field name, must not be null
     * @param fieldType the field type signature, must not be null
     * @param isStatic the flag shows that the field is static if the flag is true
     * @return a string array contains assembler commands to read the field
     */
    public abstract String [] generateFieldGetter(TranslatorContext context, String fieldName, Type fieldType, boolean isStatic);
    
    /**
     * Generate a field setter
     * @param context the translator context, must not be null 
     * @param fieldName the field name, must not be null
     * @param fieldType the field type signature, must not be null 
     * @param isStatic the flag shows that the field is a static one if the flag is true
     * @return a string array contains assembler commands to set the field
     */
    public abstract String [] generateFieldSetter(TranslatorContext context, String fieldName, Type fieldType, boolean isStatic);
    
    /**
     * The method is called once after translation and a boot class can add some assembler text in the special section if it needs
     * @return an array contains assembler text
     */
    public String [] getAdditionalText(){
        return EMPTY_STRING_ARRAY;
    }
}
