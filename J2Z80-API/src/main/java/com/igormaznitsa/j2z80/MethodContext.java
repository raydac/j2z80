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

import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import org.apache.bcel.generic.MethodGen;

/**
 * The interface describes a method context to work with methods
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface MethodContext {
    /**
     * Find a method for its method id
     * @param methodId the method id, must not be null
     * @return null if the method is not found or found MethodGen object
     */
    MethodGen findMethod(MethodID methodId);
    
    /**
     * Find the method info for the method id
     * @param methodID the method id, must not be null
     * @return null if the info is not found or a method info object if it is found
     */
    ClassMethodInfo findMethodInfo(MethodID methodID);
    
    /**
     * Find the method uid
     * @param methodId the method id to be used for search
     * @return the method uid as Integer if it is found or null
     */
    Integer findMethodUID(MethodID methodId);
}
