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
package com.igormaznitsa.j2z80.translator.jar;

import com.igormaznitsa.j2z80.utils.MutableObjectContainer;
import com.igormaznitsa.j2z80.utils.Utils;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * The Class contains methods allow to check a compiled Java class to be translated
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa,com)
 */
public enum ClassValidator {

  ;

  /**
   * Check a class to be compatible with the translator
   *
   * @param cgen a parsed compiled java class, must not be null
   * @return null if the class is compatible and a incompatibility message string if the class is not compatible
   */
  public static String validateClass(final ClassGen cgen) {
    final MutableObjectContainer<String> result = new MutableObjectContainer<>();

    return checkClassFlags(cgen, result)
        && checkConstantPool(cgen, result)
        ? null : result.get();
  }

  private static boolean checkClassFlags(final ClassGen cgen, final MutableObjectContainer<String> result) {
    if (cgen.isEnum()) {
      result.set("Enum is not supported");
    }

    return result.isNull();
  }

  private static boolean checkConstantPool(final ClassGen cgen, final MutableObjectContainer<String> result) {
    final ConstantPoolGen cpool = cgen.getConstantPool();

    for (int index = 1; index < cpool.getSize(); index++) {
      final Constant curconst = cpool.getConstant(index);

      if (curconst instanceof ConstantUtf8) {
        final String str = ((ConstantUtf8) curconst).getBytes();
        if (str.length() > 255) {
          result.set("Too long string detected, it contains more than 255 chars [" + str + ']');
          break;
        }

        for (final char chr : str.toCharArray()) {
          if ((chr & 0xFF00) != 0) {
            result.set("Detected a 16 bit coded symbol \'" + chr + "\', but only 8 bit chars allowed [\'" + str + "\']");
            break;
          }
        }
      } else if (curconst instanceof ConstantInteger) {
        final int value = ((ConstantInteger) curconst).getBytes();
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
          result.set("Integer value must be bounded in 16 bit [" + Utils.intToString(value) + ']');
          break;
        }
      } else if (curconst instanceof ConstantLong) {
        final long value = ((ConstantLong) curconst).getBytes();
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
          result.set("Long value does not fit in 32 bits [" + Utils.longToString(value) + ']');
          break;
        }
      }
    }

    return result.isNull();
  }
}
