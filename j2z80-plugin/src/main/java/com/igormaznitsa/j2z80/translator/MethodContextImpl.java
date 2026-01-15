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

import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The class allows to work with parsed methods during translation.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
class MethodContextImpl implements MethodContext {
  private final TranslatorImpl translator;
  private final Map<MethodID, ClassMethodInfo> methodIds = new HashMap<>();


  public MethodContextImpl(final TranslatorImpl translator) {
    this.translator = translator;
  }

  public List<MethodID> init() {
    int idCounter = 0;

    final List<MethodID> methodsToProcess = new ArrayList<>();

    for (final ClassGen c : this.translator.workingClassPath.getAllClasses().values()) {
      for (final Method m : c.getMethods()) {
        final MethodID methodId = new MethodID(c, m);

        this.methodIds.put(methodId, new ClassMethodInfo(c, m, idCounter));
        idCounter++;

        if (!(c.isInterface() || c.isEnum()) && !(m.isNative() || m.isAbstract())) {
          methodsToProcess.add(methodId);
        }
      }
    }

    return Collections.unmodifiableList(methodsToProcess);
  }

  Set<Entry<MethodID, ClassMethodInfo>> getMethods() {
    return methodIds.entrySet();
  }

  @Override
  public ClassMethodInfo findMethodInfo(final MethodID mid) {
    return methodIds.get(mid);
  }

  @Override
  public Integer findMethodUID(final MethodID methodId) {
    final ClassMethodInfo info = methodIds.get(methodId);
    if (info != null) {
      return info.getUID();
    }
    return null;
  }

  @Override
  public MethodGen findMethod(final MethodID methodId) {
    ClassMethodInfo info = methodIds.get(methodId);
    if (info != null) {
      return info.getMethodGen();
    }

    // find the method between ancestors because it can be inherited
    info = findInheritedMethod(methodId);
    if (info != null) {
      return info.getMethodGen();
    }
    return null;
  }

  public ClassMethodInfo findInheritedMethod(final MethodID method) {
    final String className = method.getClassName();
    ClassGen cgen = translator.workingClassPath.findClassForName(className);
    if (cgen == null) {
      return null;
    }
    cgen = translator.workingClassPath.findClassForName(cgen.getSuperclassName());

    while (cgen != null) {
      final Method compatibleMethod = method.findCompatibleMethod(cgen);
      if (compatibleMethod != null) {
        final MethodID thatMethodId = new MethodID(cgen, compatibleMethod);
        return methodIds.get(thatMethodId);
      }
      cgen = translator.workingClassPath.findClassForName(cgen.getSuperclassName());
    }
    return null;
  }

}
