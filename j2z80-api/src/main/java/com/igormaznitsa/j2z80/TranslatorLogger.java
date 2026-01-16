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

/**
 * The interface describes a logger to be used inbound of the translator.
 *
 * @author Igor Maznutsa (igor.maznitsa@igormaznitsa.com)
 */
public interface TranslatorLogger {
  /**
   * Print an information message
   *
   * @param s an information message, it can be null
   */
  void logInfo(String s);

  /**
   * Print a warning message
   *
   * @param s a warning message, it can be null
   */
  void logWarning(String s);

  /**
   * Print debug message
   *
   * @param s a debug message, it can be null
   */
  void logDebug(String s);

  /**
   * Print an error message
   *
   * @param s an error message, it can be null
   */
  void logError(String s);
}
