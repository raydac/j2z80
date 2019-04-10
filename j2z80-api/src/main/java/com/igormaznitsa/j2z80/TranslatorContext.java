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
package com.igormaznitsa.j2z80;

import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.MethodID;
import java.io.IOException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.generic.Type;

/**
 * The interface describes the translator context
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface TranslatorContext {
    /**
     * The main method name.
     */
    public static final String Z80_MAIN_METHOD_NAME = "mainz";
    
    /**
     * The main method signature.
     */
    public static final String Z80_MAIN_METHOD_SIGNATURE = Type.getMethodSignature(Type.VOID, new Type[0]);

    /**
     * Translate compiled java classes into assembler text
     * @param mainClassName the main class name, it can be null
     * @param startAddress the start address for translation
     * @param stackTopAddress the stack top address to be used by the compiled code
     * @param excludeBinResPatterns patterns to be used to exclude met resources in JAR files
     * @return assembler text of translated Java classes
     * @throws IOException it will be thrown if there is any transport problem
     */
    String[] translate(String mainClassName, int startAddress, int stackTopAddress, String [] excludeBinResPatterns) throws IOException; 

    /**
     * Get the current class context
     * @return the current class context
     */
    ClassContext getClassContext();
    
    /**
     * Get the current method context
     * @return the current method context
     */
    MethodContext getMethodContext();
    
    /**
     * Get the logger for the context
     * @return the logger for the context
     */
    TranslatorLogger getLogger();

    /**
     * Register additions needed by a class
     * @param classToCheck the class contains additions in its definition
     */
    void registerAdditionsUsedByClass (Class<?> classToCheck); 
    
    /**
     * Register a class id to be used in cast check operations
     * @param classId the class id object of the class, must not be null
     * @return the uid of the class as Integer or null if it can't be found 
     */
    Integer registerClassForCastCheck(ClassID classId);
    
    /**
     * Register a method id for invokeinterface operations
     * @param methodId the method id to be registered, must not be null
     * @return the uid of the method as Integer or null if it can't be found
     */
    Integer registerInterfaceMethodForINVOKEINTERFACE(MethodID methodId);
    
    /**
     * Register a constant pool item to be translated
     * @param constantLabel the label for the constant, must not be null
     * @param item the constant pool item to be registered, must not be null
     */
    void registerConstantPoolItem(String constantLabel, Constant item);
    
    /**
     * Register a boot class processor to be translated
     * @param classProcessor a boot class processor, must not be null
     */
    void registerCalledBootClassProcesser(AbstractBootClass classProcessor);

    /**
     * Load a resource from the inside virtual translation space
     * @param path the path of the resource, must not be null
     * @return the found resource as a byte array or null if it is not found
     * @throws IOException it will be thrown if there is any problem on the transport level
     */
    byte[] loadResourceForPath(final String path) throws IOException; 

}
