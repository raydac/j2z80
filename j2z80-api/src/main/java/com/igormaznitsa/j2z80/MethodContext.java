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

import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import org.apache.bcel.generic.MethodGen;

/**
 * The interface describes a method context to work with class methods.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface MethodContext {
  /**
   * Find a method for its method id
   *
   * @param methodId the method id, must not be null
   * @return null if the method is not found or found MethodGen object
   */
  MethodGen findMethod(MethodID methodId);

  /**
   * Find the method info for the method id
   *
   * @param methodID the method id, must not be null
   * @return null if the info is not found or a method info object if it is found
   */
  ClassMethodInfo findMethodInfo(MethodID methodID);

  /**
   * Find the method uid
   *
   * @param methodId the method id to be used for search
   * @return the method uid as Integer if it is found or null
   */
  Integer findMethodUID(MethodID methodId);
}
