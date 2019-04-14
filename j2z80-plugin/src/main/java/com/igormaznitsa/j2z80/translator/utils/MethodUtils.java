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
package com.igormaznitsa.j2z80.translator.utils;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

public class MethodUtils {
  private MethodUtils() {
  }

  public static boolean isStaticInitializer(final Method method) {
    return method.isStatic() && method.getName().equals("<clinit>")
        && method.getArgumentTypes().length == 0
        && method.getReturnType().getType() == Type.VOID.getType();
  }


}
