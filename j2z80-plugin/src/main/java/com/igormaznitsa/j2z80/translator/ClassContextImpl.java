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

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The Class implements a class context handler
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
class ClassContextImpl implements ClassContext {

  private final TranslatorImpl theTranslator;
  private final Map<ClassID, ClassMethodInfo> mapClassId = new HashMap<>();
  private final Set<ClassID> setClassesWithJNI = new HashSet<>();

  public ClassContextImpl(final TranslatorImpl translator) {
    this.theTranslator = translator;
  }

  void init() {
    int classIdCounter = 0;

    // map all classes from the translator and generate their ids
    for (final ClassGen c : theTranslator.workingClassPath.getAllClasses().values()) {
      final ClassID classId = new ClassID(c);
      final ClassMethodInfo classInfo = new ClassMethodInfo(c, null, classIdCounter);
      mapClassId.put(classId, classInfo);

      if (!c.isInterface()) {
        for (final Method m : c.getMethods()) {
          if (m.isNative()) {
            setClassesWithJNI.add(classId);
            break;
          }
        }
      }

      classIdCounter++;
    }
  }

  Set<ClassID> getClassesWithDetectedJNI() {
    return setClassesWithJNI;
  }

  Set<Entry<ClassID, ClassMethodInfo>> getAllRegisteredClasses() {
    return mapClassId.entrySet();
  }

  @Override
  public Integer findClassUID(final ClassID classId) {
    final ClassMethodInfo info = mapClassId.get(classId);
    if (info != null) {
      return info.getUID();
    }
    return null;
  }

  @Override
  public Set<ClassID> findAllClassesImplementInterface(final String interfaceName) {
    final Set<ClassID> result = new HashSet<>();

    final ClassGen classGen = findClassForID(new ClassID(interfaceName));
    if (classGen.isInterface()) {

      for (final ClassGen cgen : theTranslator.workingClassPath.getAllClasses().values()) {
        for (final String name : cgen.getInterfaceNames()) {
          if (interfaceName.equals(name)) {
            if (cgen.isInterface()) {
              final Set<ClassID> thatInterface = findAllClassesImplementInterface(name);
              result.addAll(thatInterface);
            } else {
              result.add(new ClassID(cgen));

              final List<String> successors = findAllClassSuccessors(cgen.getClassName());
              if (!successors.isEmpty()) {
                for (final String className : successors) {
                  result.add(new ClassID(className));
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public Iterable<ClassID> getAllClasses() {
    return mapClassId.keySet();
  }

  ClassMethodInfo findClassInfoForID(final ClassID classID) {
    return mapClassId.get(classID);
  }

  @Override
  public ClassGen findClassForID(final ClassID classID) {
    final ClassMethodInfo classMethodInfo = mapClassId.get(classID);
    return classMethodInfo == null ? null : classMethodInfo.getClassInfo();
  }

  @Override
  public boolean isAccessible(final ClassGen classGen, final String superClassName) {
    ClassGen tmpClassGen = classGen;
    if (superClassName.equals(tmpClassGen.getClassName())) {
      return true;
    }
    while (tmpClassGen != null) {
      final String superCName = tmpClassGen.getSuperclassName();
      if (superClassName.equals(superCName)) {
        return true;
      }

      tmpClassGen = theTranslator.workingClassPath.findClassForName(superCName);
    }
    return false;
  }

  @Override
  public List<String> findAllClassAncestors(final String className) {
    final List<String> result = new ArrayList<>();

    String tmpClassName = className;

    while (!"java.lang.Object".equals(tmpClassName)) {
      if (!className.equals(tmpClassName)) {
        result.add(tmpClassName);
      }
      final ClassGen classGen = findClassForID(new ClassID(tmpClassName));
      tmpClassName = classGen.getSuperclassName();
    }
    return result;
  }

  @Override
  public List<String> findAllClassSuccessors(final String className) {
    final List<String> result = new ArrayList<>();
    for (final ClassGen cls : theTranslator.workingClassPath.getAllClasses().values()) {
      if (className.equals(cls.getClassName())) {
        continue;
      }
      if (isAccessible(cls, className)) {
        result.add(cls.getClassName());
      }
    }
    return result;
  }
}
