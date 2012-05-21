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

public interface TranslatorContext {
    public static final String Z80_MAIN_METHOD_NAME = "mainz";
    public static final String Z80_MAIN_METHOD_SIGNATURE = Type.getMethodSignature(Type.VOID, new Type[0]);

    String[] translate(String mainClassName, int startAddress, int stackTopAddress, String [] excludeBinResPatterns) throws IOException; 

    ClassContext getClassContext();
    MethodContext getMethodContext();
    TranslatorLogger getLogger();

    void registerAdditionsUsedByClass (Class<?> classToCheck);  
    Integer registerClassForCastCheck(ClassID classId);
    Integer registerInterfaceMethodForINVOKEINTERFACE(MethodID methodId);
    void registerConstantPoolItem(String result, Constant item);
    void registerCalledBootClassProcesser(AbstractBootClass classProcessor);

    byte[] loadResourceForPath(final String path) throws IOException; 

}
