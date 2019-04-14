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
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.MethodGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InvokeinterfaceTable {
  private final TranslatorContext translator;
  private final List<Record> records = new ArrayList<>();

  public InvokeinterfaceTable(final TranslatorContext translator, final Set<MethodID> calledInterfaceMethods) {
    this.translator = translator;
    final ClassContext classContext = translator.getClassContext();
    for (final MethodID method : calledInterfaceMethods) {
      final Set<ClassID> successors = classContext.findAllClassesImplementInterface(method.getClassName());
      final Record newRecord = new Record(method);
      for (final ClassID s : successors) {
        newRecord.addInheritedMethod(new MethodID(s.getClassName(), method.getMethodName(), method.getReturnType(), method.getArgs()));
      }
      records.add(newRecord);
    }
  }

  public String generateAsm() {
    final StringBuilder result = new StringBuilder("; GENERATED INVOKEINTERFACE TABLE\nDEFB ");
    result.append(records.size()).append('\n');
    for (final Record r : records) {
      result.append(r.generateAsm());
    }
    return result.toString();
  }

  private class Record {
    private final MethodID interfaceMethod;
    private final List<MethodID> inheritedMethods = new ArrayList<>();

    private Record(final MethodID interfaceMethod) {
      this.interfaceMethod = interfaceMethod;
    }

    private void addInheritedMethod(final MethodID method) {
      inheritedMethods.add(method);
    }

    private String generateAsm() {
      final StringBuilder result = new StringBuilder("DEFW ");

      result.append(LabelAndFrameUtils.makeLabelForMethodID(interfaceMethod)).append('\n');

      result.append("DEFB ").append(inheritedMethods.size()).append('\n');

      for (final MethodID method : inheritedMethods) {
        final MethodGen meth = translator.getMethodContext().findMethod(method);
        final int frameSize = LabelAndFrameUtils.calculateFrameSizeForMethod(meth.getArgumentTypes().length, meth.getMaxLocals(), false);

        result.append("DEFW ").append(LabelAndFrameUtils.makeLabelForClassID(method.getClassID())).append(',')
            .append(method.getMethodLabel()).append(',')
            .append(frameSize)
            .append('\n');

      }
      return result.toString();
    }
  }
}
