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

package com.igormaznitsa.j2z80.ids;

import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelNameForClass;
import static java.util.Objects.requireNonNull;

import org.apache.bcel.generic.ClassGen;

/**
 * Immutable identity for a Java class as it is known to the translator. The value is the
 * canonical class name and is used to compare classes, generate stable labels, and map runtime
 * metadata such as cast checks and virtual dispatch tables.
 *
 * <p>Two {@code ClassID} instances are equal when they describe the same canonical class name,
 * regardless of where they were created.</p>
 */
public class ClassID {
  // inside storage of the full class name
  private final String className;

  /**
   * Creates a new class identity from the canonical Java class name.
   *
   * @param className the fully qualified class name, for example {@code java.lang.String},
   *                  must not be {@code null} or blank
   */
  public ClassID(final String className) {
    requireNonNull(className, "Class name must not be null");
    if (className.isBlank()) {
      throw new IllegalArgumentException("Class name must not be blank");
    }
    this.className = className;
  }

  /**
   * Creates a new class identity from a BCEL class definition.
   *
   * @param classGen the class metadata to wrap, must not be {@code null}
   */
  public ClassID(final ClassGen classGen) {
    requireNonNull(classGen, "Argument must not be null");
    this.className = classGen.getClassName();
  }

  @Override
  public int hashCode() {
    return this.className.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof ClassID) {
      return this.className.equals(((ClassID) obj).className);
    }
    return false;
  }

  /**
   * Returns the canonical class name represented by this identity.
   *
   * @return the fully qualified Java class name
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Generates the internal assembly label that corresponds to this class.
   *
   * @return a normalized Z80 label name for the represented class
   */
  public String makeClassLabel() {
    return makeLabelNameForClass(this.className);
  }

  @Override
  public String toString() {
    return this.className;
  }
}
