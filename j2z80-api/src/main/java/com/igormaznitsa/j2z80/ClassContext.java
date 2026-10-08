/*
 * Copyright 2012-2026 Igor Maznitsa.
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
import java.util.List;
import java.util.Set;
import org.apache.bcel.generic.ClassGen;

/**
 * Maintains the class graph and metadata used by the translator while it resolves inheritance,
 * interfaces, and runtime identities. Through this context, generated code can ask which classes
 * are reachable, which interfaces they implement, and which runtime identifiers have already been
 * assigned.
 *
 * @author Igoe Manzitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface ClassContext {
  /**
   * Returns all translated Java classes currently known to the translator.
   *
   * @return an iterable over class identities, never {@code null}
   */
  Iterable<ClassID> getAllClasses();

  /**
   * Resolves the full inheritance chain for a class name.
   *
   * @param className the canonical class name to inspect, must not be {@code null}
   * @return the ordered list of ancestor class names, beginning with the class itself and ending
   *         with the root type; an empty list is returned when the class is unknown
   */
  List<String> findAllClassAncestors(final String className);

  /**
   * Finds every interface implemented by a class, including interfaces inherited through its
   * ancestors.
   *
   * @param className the canonical class name to inspect, must not be {@code null}
   * @return the set of interface identifiers implemented by the class and its ancestors
   */
  Set<ClassID> findAllClassesImplementInterface(String className);

  /**
   * Retrieves the BCEL description for a class identifier.
   *
   * @param classId the class identifier to locate, must not be {@code null}
   * @return the matching {@link ClassGen}, or {@code null} if no such class is registered
   */
  ClassGen findClassForID(ClassID classId);

  /**
   * Lists all known class successors reachable from the given class in the current hierarchy.
   *
   * @param className the canonical class name to inspect, must not be {@code null}
   * @return the list of direct and indirect subtype names in the translator's current graph
   */
  List<String> findAllClassSuccessors(String className);

  /**
   * Retrieves the runtime identifier assigned to the given class.
   *
   * @param classId the class identifier to inspect, must not be {@code null}
   * @return the assigned class ID, or {@code null} if the class has not been registered
   */
  Integer findClassUID(ClassID classId);

  /**
   * Checks whether a superclass reference is valid in the current translated type graph.
   *
   * @param classInfo the class metadata being checked, must not be {@code null}
   * @param superClassName the canonical superclass name to validate, must not be {@code null}
   * @return {@code true} when the superclass is accessible to the given class, otherwise
   *         {@code false}
   */
  boolean isAccessible(ClassGen classInfo, String superClassName);

}
