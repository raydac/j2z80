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

import com.igormaznitsa.j2z80.ids.ClassID;
import java.util.List;
import java.util.Set;
import org.apache.bcel.generic.ClassGen;

/**
 * The interface describes context to work with classes
 * 
 * @author Igoe Manzitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface ClassContext {
    /**
     * Get iterator for all translated java classes
     * @return an iterator for all translated java classes
     */
    Iterable<ClassID> getAllClasses();
    
    /**
     * Find all class ancestors
     * @param className the canonical class name, must not be null
     * @return the list contains class names of the class ancestors
     */
    List<String> findAllClassAncestors(final String className);
    
    /**
     * Find all interfaces which are implemented by the class (also it finds interfaces implemented by all class ancestors)
     * @param className the canonical class name, must not be null
     * @return the list of interfaces which are implemented by the class or one of its ancestor
     */
    Set<ClassID> findAllClassesImplementInterface(String className);
    
    /**
     * Find a ClassGen for class id
     * @param classId the class id object to be used in the search, must not be null
     * @return found ClassGen or null
     */
    ClassGen findClassForID(ClassID classId);
    
    /**
     * Find all successors for a class
     * @param className the canonical class name, must not be null
     * @return a list contains all found successors of the class
     */
    List<String> findAllClassSuccessors(String className);
    
    /**
     * Find the class uid for the class id
     * @param classId a class id object, must not be null
     * @return found UID as Integer or null
     */
    Integer findClassUID(ClassID classId);
    
    /**
     * Check that a super class is accessible from a ClassGen object
     * @param classInfo the class gen object, must not be null
     * @param superClassName the canonical class name of the superclass to be checked for accessibility
     * @return true if the superclass is accessible from the class gen object
     */
    boolean isAccessible(ClassGen classInfo, String superClassName);

}
