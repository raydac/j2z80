/* 
 * Copyright 2019 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.j2z80;

import com.igormaznitsa.j2z80.ids.ClassID;
import org.apache.bcel.generic.ClassGen;

import java.util.List;
import java.util.Set;

/**
 * The interface describes context to work with classes
 *
 * @author Igoe Manzitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface ClassContext {
  /**
   * Get iterator for all translated java classes
   *
   * @return an iterator for all translated java classes
   */
  Iterable<ClassID> getAllClasses();

  /**
   * Find all class ancestors
   *
   * @param className the canonical class name, must not be null
   * @return the list contains class names of the class ancestors
   */
  List<String> findAllClassAncestors(final String className);

  /**
   * Find all interfaces which are implemented by the class (also it finds interfaces implemented by all class ancestors)
   *
   * @param className the canonical class name, must not be null
   * @return the list of interfaces which are implemented by the class or one of its ancestor
   */
  Set<ClassID> findAllClassesImplementInterface(String className);

  /**
   * Find a ClassGen for class id
   *
   * @param classId the class id object to be used in the search, must not be null
   * @return found ClassGen or null
   */
  ClassGen findClassForID(ClassID classId);

  /**
   * Find all successors for a class
   *
   * @param className the canonical class name, must not be null
   * @return a list contains all found successors of the class
   */
  List<String> findAllClassSuccessors(String className);

  /**
   * Find the class uid for the class id
   *
   * @param classId a class id object, must not be null
   * @return found UID as Integer or null
   */
  Integer findClassUID(ClassID classId);

  /**
   * Check that a super class is accessible from a ClassGen object
   *
   * @param classInfo      the class gen object, must not be null
   * @param superClassName the canonical class name of the superclass to be checked for accessibility
   * @return true if the superclass is accessible from the class gen object
   */
  boolean isAccessible(ClassGen classInfo, String superClassName);

}
