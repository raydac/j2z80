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
import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The class describes an invoke virtual table.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class InvokeVirtualTable {

  private final Map<String, Record> allVirtualMethods = new HashMap<>();
  private final TranslatorContext translator;

  public InvokeVirtualTable(final TranslatorContext translator) {
    this.translator = translator;

    processClassesWithVirtualMethods();
  }

  private void processClassesWithVirtualMethods() {
    for (final ClassID cg : translator.getClassContext().getAllClasses()) {
      processClass(cg);
    }
  }

  private void processClass(final ClassID classId) {
    final ClassGen classGen = translator.getClassContext().findClassForID(classId);

    if (classGen.isInterface()) {
      return;
    }

    final Map<String, ClassMethodInfo> visibleMethods =
        collectAllVisibleVirtualMethodsToRoot(classGen, new HashMap<>());

    if (!visibleMethods.isEmpty()) {
      makeRecordsForVirtualMethods(classGen, visibleMethods.values());
    }
  }

  private void makeRecordsForVirtualMethods(final ClassGen classGen, final Collection<ClassMethodInfo> methods) {
    final ClassContext classContext = translator.getClassContext();

    final List<String> allAncestors = new ArrayList<>(classContext.findAllClassAncestors(classGen.getClassName()));
    allAncestors.add(classGen.getClassName());

    for (final String className : allAncestors) {
      final ClassGen ancestor = classContext.findClassForID(new ClassID(className));

      for (final ClassMethodInfo m : methods) {
        final Record record = findRecord(new ClassID(ancestor.getClassName()), m.getMethodInfo());

        if (!m.getMethodInfo().isAbstract() && classContext.isAccessible(m.getClassInfo(), ancestor.getClassName())) {
          record.addCase(m);
        }
      }
    }
  }

  private Record findRecord(final ClassID cid, final Method method) {
    final String recordUid = cid.getClassName() + "!" + makeMethodUID(method);
    Record record = this.allVirtualMethods.get(recordUid);
    if (record == null) {
      record = new Record(cid, method);
      this.allVirtualMethods.put(recordUid, record);
    }
    return record;
  }

  private String makeMethodUID(final Method method) {
    return method.getName() + method.getSignature();
  }

  private Map<String, ClassMethodInfo> collectAllVisibleVirtualMethodsToRoot(
      final ClassGen classGen, final Map<String, ClassMethodInfo> map) {
    for (final Method m : classGen.getMethods()) {
      if (m.isStatic() || m.getName().equals("<init>") || m.isAbstract()) {
        continue;
      }

      final String textMethodUid = this.makeMethodUID(m);
      if (!map.containsKey(textMethodUid)) {
        final MethodID methodId = new MethodID(classGen, m);
        final ClassMethodInfo classMethodInfo =
            this.translator.getMethodContext().findMethodInfo(methodId);
        if (classMethodInfo == null) {
          throw new IllegalStateException("Can't find method info: " + methodId);
        }
        map.put(textMethodUid, classMethodInfo);
      }
    }

    if ("java.lang.Object".equals(classGen.getSuperclassName())) {
      return map;
    }

    return this.collectAllVisibleVirtualMethodsToRoot(
        this.translator.getClassContext().findClassForID(new ClassID(classGen.getSuperclassName())),
        map);
  }

  public String toAsm() {
    final StringBuilder result = new StringBuilder("; GENERATED INVOKEVIRTUAL TABLE\n");

    for (final Record r : this.allVirtualMethods.values()) {
      final Method method = r.method;
      // make record label
      result.append(LabelAndFrameUtils.makeLabelForVirtualMethodRecord(r.classId.getClassName(),
          method.getName(), method.getReturnType(), method.getArgumentTypes())).append(":\n");
      // records
      result.append(r.toAsm());
    }

    return result.toString();
  }

  /**
   * The class describes a record in a invoke virtual table
   *
   * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
   */
  private class Record {

    private final ClassID classId;
    private final Method method;
    private final Set<ClassMethodInfo> cases = new HashSet<>();

    private Record(final ClassID classId, final Method method) {
      this.classId = classId;
      this.method = method;
    }

    private void addCase(final ClassMethodInfo classMethodInfo) {
      this.cases.add(classMethodInfo);
    }

    private String toAsm() {
      final StringBuilder result = new StringBuilder();
      result.append("DEFB ").append(this.cases.size()).append('\n');

      for (final ClassMethodInfo r : this.cases) {
        result.append(generateAsmForMethod(r));
      }

      return result.toString();
    }

    private String generateAsmForMethod(final ClassMethodInfo classMethodInfo) {
      final MethodGen methGen = classMethodInfo.getMethodGen();
      final String methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(classMethodInfo);
      final int classId =
          translator.getClassContext().findClassUID(new ClassID(classMethodInfo.getClassInfo()));
      final int frameSize = LabelAndFrameUtils.calculateFrameSizeForMethod(methGen.getArgumentTypes().length, methGen.getMaxLocals(), false);

      return "DEFW " + classId + ','
          + methodLabel + ','
          + frameSize + '\n';
    }
  }
}
