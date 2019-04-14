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
package com.igormaznitsa.j2z80.ids;

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import org.apache.bcel.generic.ClassGen;

/**
 * CLASS ID which is being used by the translator to identify a java class during processing.
 */
public class ClassID {
  // inside storage of the full class name
  private final String className;

  /**
   * The Constructor creates the new instance based on the full class path name
   *
   * @param className the full canonical class path name, must not be null
   */
  public ClassID(final String className) {
    Assertions.assertNotNull("Class name must not be null", className);
    Assertions.assertFalse("Class name must not be empty", className.isEmpty());
    this.className = className;
  }

  /**
   * The Constructor create the new instance based on a ClassGet object
   *
   * @param classGen the object to be used for creation, must not be null
   */
  public ClassID(final ClassGen classGen) {
    Assertions.assertNotNull("Argument must not be null", classGen);
    className = classGen.getClassName();
  }

  @Override
  public int hashCode() {
    return className.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof ClassID) {
      final ClassID meth = (ClassID) obj;
      return className.equals(meth.className);
    }
    return false;
  }

  /**
   * Get the full class name
   *
   * @return the full class name as String
   */
  public String getClassName() {
    return className;
  }

  /**
   * Make the label for the class
   *
   * @return a String contains the label for the class name
   */
  public String makeClassLabel() {
    return LabelAndFrameUtils.makeLabelNameForClass(className);
  }

  @Override
  public String toString() {
    return className;
  }
}
