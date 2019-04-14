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
package com.igormaznitsa.j2z80.translator.optimizator;

import java.util.Locale;

/**
 * List of allowed optimization levels
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public enum OptimizationLevel {
  /**
   * Don't make any optimization
   */
  NONE("none"),

  /**
   * Make low optimization, mainly remove meaningless command pairs
   */
  BASE("base");

  // save the text name for the optimization level
  private final String textName;

  OptimizationLevel(final String textName) {
    this.textName = textName;
  }

  /**
   * Find an optimization level for its human name
   *
   * @param textName the human name of the needed optimization level
   * @return found optimization level or NONE if such level is not found for its text name
   */
  public static OptimizationLevel findForTextName(final String textName) {
    final String textNameInLowCase = textName.toLowerCase(Locale.ENGLISH);
    for (final OptimizationLevel value : values()) {
      if (value.getTextName().toLowerCase(Locale.ENGLISH).equals(textNameInLowCase)) {
        return value;
      }
    }
    return NONE;
  }

  /**
   * Get the human level name
   *
   * @return the level name as String
   */
  public String getTextName() {
    return this.textName;
  }
}
