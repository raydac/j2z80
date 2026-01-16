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

import static com.igormaznitsa.j2z80.translator.utils.ClassUtils.hasJ2Z80Ignore;

import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public List<MethodID> findMethodsForProcessingInClassPath() {
    int idCounter = 0;

    final List<MethodID> methodsToProcess = new ArrayList<>();
    for (final ClassGen classGen : this.translator.workingClassPath.getAllClasses().values()) {
      if (hasJ2Z80Ignore(classGen.getAttributes())) {
        continue;
      }

      for (final Method method : ClassUtils.findBoostrapAwareMethods(classGen)) {
        final MethodID methodId = new MethodID(classGen, method);

        this.methodIds.put(methodId, new ClassMethodInfo(classGen, method, idCounter));
        idCounter++;

        if (!(classGen.isInterface() || classGen.isEnum()) &&
            !(method.isNative() || method.isAbstract())) {
          methodsToProcess.add(methodId);
        }
      }
    }

    return Collections.unmodifiableList(methodsToProcess);
  }

  @Override
  public Map<MethodID, ClassMethodInfo> getMethods() {
    return this.methodIds;
  }

  @Override
  public ClassMethodInfo findMethodInfo(final MethodID mid) {
    return this.methodIds.get(mid);
  }

  @Override
  public Integer findMethodUID(final MethodID methodId) {
    final ClassMethodInfo info = this.methodIds.get(methodId);
    if (info != null) {
      return info.getUID();
    }
    return null;
  }

  @Override
  public MethodGen findMethod(final MethodID methodId) {
    ClassMethodInfo info = this.methodIds.get(methodId);
    if (info != null) {
      return info.getMethodGen();
    }

    // find the method between ancestors because it can be inherited
    info = this.findInheritedMethod(methodId);
    return info == null ? null : info.getMethodGen();
  }

  public ClassMethodInfo findInheritedMethod(final MethodID method) {
    final String className = method.getClassName();
    ClassGen cgen = this.translator.workingClassPath.findClassForName(className);
    if (cgen == null) {
      return null;
    }
    cgen = this.translator.workingClassPath.findClassForName(cgen.getSuperclassName());

    while (cgen != null) {
      final Method compatibleMethod = method.findCompatibleMethod(cgen);
      if (compatibleMethod != null) {
        final MethodID thatMethodId = new MethodID(cgen, compatibleMethod);
        return this.methodIds.get(thatMethodId);
      }
      cgen = this.translator.workingClassPath.findClassForName(cgen.getSuperclassName());
    }
    return null;
  }

}
