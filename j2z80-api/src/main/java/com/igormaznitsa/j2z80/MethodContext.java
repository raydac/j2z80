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

import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import java.util.Map;
import org.apache.bcel.generic.MethodGen;

/**
 * Tracks the Java methods that have already been analyzed or registered within the current
 * translation session. This registry allows generated code and runtime support code to resolve a
 * method signature back to its owning class, metadata, and unique runtime identifier.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface MethodContext {
  /**
   * Finds a method definition by its unique method identity.
   *
   * @param methodId the method identity to look up, must not be {@code null}
   * @return the matching method definition as a BCEL {@link MethodGen}, or {@code null} if the
   *         method is not registered
   */
  MethodGen findMethod(MethodID methodId);

  /**
   * Finds the metadata object associated with a method identity.
   *
   * @param methodID the method identity to look up, must not be {@code null}
   * @return the method metadata, or {@code null} if the method is not known to the translator
   */
  ClassMethodInfo findMethodInfo(MethodID methodID);

  /**
   * Returns all registered methods in the current translation context.
   *
   * @return an immutable view or snapshot of the method registry, keyed by method identity
   */
  Map<MethodID, ClassMethodInfo> getMethods();

  /**
   * Resolves the runtime identifier assigned to a method.
   *
   * @param methodId the method identity to inspect, must not be {@code null}
   * @return the numeric method ID, or {@code null} if the method has not been registered
   */
  Integer findMethodUID(MethodID methodId);
}
