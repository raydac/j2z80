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
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.MethodGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class describes a invoke virtual table.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class InvokevirtualTable {

  private final Map<String, Record> allVirtualMethods = new HashMap<>();
  private final TranslatorContext theTranslator;

  public InvokevirtualTable(final TranslatorContext translator) {
    theTranslator = translator;

    processClassesWithVirtualMethods();
  }

  private void processClassesWithVirtualMethods() {
    for (final ClassID cg : theTranslator.getClassContext().getAllClasses()) {
      processClass(cg);
    }
  }

  private void processClass(final ClassID classId) {
    final ClassGen classGen = theTranslator.getClassContext().findClassForID(classId);

    if (classGen.isInterface()) {
      return;
    }

    final Map<String, ClassMethodInfo> visibleMethods = collectAllVisibledVirtualMethodsToRoot(classGen, new HashMap<>());

    if (!visibleMethods.isEmpty()) {
      makeRecordsForVirtualMethods(classGen, visibleMethods.values());
    }
  }

  private void makeRecordsForVirtualMethods(final ClassGen classGen, final Collection<ClassMethodInfo> methods) {
    final ClassContext classContext = theTranslator.getClassContext();

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
    final String recorduid = cid.getClassName() + "!" + makeMethodUID(method);
    Record record = allVirtualMethods.get(recorduid);
    if (record == null) {
      record = new Record(cid, method);
      allVirtualMethods.put(recorduid, record);
    }
    return record;
  }

  private String makeMethodUID(final Method method) {
    return method.getName() + method.getSignature();
  }

  private Map<String, ClassMethodInfo> collectAllVisibledVirtualMethodsToRoot(final ClassGen classGen, final Map<String, ClassMethodInfo> accum) {
    if (classGen.isInterface()) {
      return accum;
    }

    for (final Method m : classGen.getMethods()) {
      if (m.isStatic() || m.getName().equals("<init>") || m.isPrivate()) {
        continue;
      }

      final String muid = makeMethodUID(m);
      if (!accum.containsKey(muid)) {
        accum.put(muid, theTranslator.getMethodContext().findMethodInfo(new MethodID(classGen, m)));
      }
    }

    if ("java.lang.Object".equals(classGen.getSuperclassName())) {
      return accum;
    }

    return collectAllVisibledVirtualMethodsToRoot(theTranslator.getClassContext().findClassForID(new ClassID(classGen.getSuperclassName())), accum);
  }

  public String toAsm() {
    final StringBuilder result = new StringBuilder("; GENERATED INVOKEVIRTUAL TABLE\n");

    for (final Record r : allVirtualMethods.values()) {
      final Method method = r.theMethod;
      // make record label
      result.append(LabelAndFrameUtils.makeLabelForVirtualMethodRecord(r.theClassId.getClassName(), method.getName(), method.getReturnType(), method.getArgumentTypes())).append(":\n");
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

    private final ClassID theClassId;
    private final Method theMethod;
    private final Set<ClassMethodInfo> cases = new HashSet<>();

    private Record(final ClassID classId, final Method method) {
      theClassId = classId;
      theMethod = method;
    }

    private void addCase(final ClassMethodInfo classMethodInfo) {
      cases.add(classMethodInfo);
    }

    private String toAsm() {
      final StringBuilder result = new StringBuilder();

      result.append("DEFB ").append(cases.size()).append('\n');

      for (final ClassMethodInfo r : cases) {
        result.append(generateAsmForMethod(r));
      }

      return result.toString();
    }

    private String generateAsmForMethod(final ClassMethodInfo classMethodInfo) {
      final MethodGen methGen = classMethodInfo.getMethodGen();
      final String methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(classMethodInfo);
      final int classId = theTranslator.getClassContext().findClassUID(new ClassID(classMethodInfo.getClassInfo()));
      final int frameSize = LabelAndFrameUtils.calculateFrameSizeForMethod(methGen.getArgumentTypes().length, methGen.getMaxLocals(), false);

      return "DEFW " + classId + ','
          + methodLabel + ','
          + frameSize + '\n';
    }
  }
}
