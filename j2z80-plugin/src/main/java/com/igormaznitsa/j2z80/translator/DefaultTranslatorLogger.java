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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorLogger;

/**
 * The class implements a logger to be used by a translator and it prints
 * messages into System streams
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class DefaultTranslatorLogger implements TranslatorLogger {

  private static final String DEFAULT_LOG_PREFIX = "J2Z [%1$s] -> %2$s";

  @Override
  public void logInfo(final String message) {
    if (System.out != null) {
      System.out.println(String.format(DEFAULT_LOG_PREFIX, "INFO", message));
    }
  }

  @Override
  public void logWarning(final String message) {
    if (System.out != null) {
      System.out.println(String.format(DEFAULT_LOG_PREFIX, "WARN", message));
    }
  }

  @Override
  public void logError(final String message) {
    if (System.err != null) {
      System.err.println(String.format(DEFAULT_LOG_PREFIX, "ERR", message));
    }
  }
}
