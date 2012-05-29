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
package com.igormaznitsa.j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import org.apache.bcel.generic.Type;

/**
 * The class implements stub for the java.lang.Object class from the standard Java framework.
 * At present it supports only the constructor and the hashCode method.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class Object extends AbstractBootClass {

    @Override
    public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName, final Type[] methodArguments, final Type resultType) {
        return false;
    }

    @Override
    public String[] generateInvocation(final TranslatorContext translator, final String methodName, final Type[] methodArguments, final Type resultType) {
        if (methodArguments.length == 0){
            if (resultType.getType() == Type.VOID.getType()){
                if (methodName.equals("<init>")){
                    return new String[]{"POP BC ; call of Object.<init>, just drop the reference"};
                }
            } else if (resultType.getType() == Type.INT.getType()){
                if (methodName.equals("hashCode")){
                    return new String[]{"; hashCode for Object just returns the object address which already on the stack"};
                }
            }
        }
        throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
        return null;
    }

    @Override
    public String[] generateFieldGetter(final TranslatorContext context, final String fieldName, final Type fieldType, final boolean isStatic) {
        throwBootClassExceptionForField(fieldName, fieldType);
        return null;
    }

    @Override
    public String[] generateFieldSetter(final TranslatorContext context, final String fieldName, final Type fieldType, final boolean isStatic) {
        throwBootClassExceptionForField(fieldName, fieldType);
        return null;
    }

 }
