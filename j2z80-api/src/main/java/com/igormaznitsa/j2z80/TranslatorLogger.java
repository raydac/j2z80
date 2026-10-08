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

/**
 * Receiver for translator diagnostics emitted while a Java class is being compiled into
 * Z80 assembly. Integrators can implement this interface to log information, warnings,
 * debugging output, or recoverable and fatal translation errors produced by the translator.
 *
 * <p>The logger is intentionally small and side-effect free apart from the consumer's own
 * output sink. Implementations are expected to write every message to a destination that is
 * appropriate for the calling environment, such as a console, build log, or IDE diagnostics
 * panel.</p>
 *
 * @author Igor Maznutsa (igor.maznitsa@igormaznitsa.com)
 */
public interface TranslatorLogger {
  /**
   * Logs a normal informational message generated during translation.
   *
   * @param s the message text, or {@code null} if the translator provides no details
   */
  void logInfo(String s);

  /**
   * Logs a non-fatal condition that indicates a potential problem in the generated output or
   * source code mapping.
   *
   * @param s the warning text, or {@code null} if no warning text is available
   */
  void logWarning(String s);

  /**
   * Logs diagnostic output intended for debugging a translation issue.
   *
   * @param s the debug text, or {@code null} if no debug text is available
   */
  void logDebug(String s);

  /**
   * Logs a translation or assembly error that may prevent the generated code from being used.
   *
   * @param s the error text, or {@code null} if no error text is available
   */
  void logError(String s);
}
