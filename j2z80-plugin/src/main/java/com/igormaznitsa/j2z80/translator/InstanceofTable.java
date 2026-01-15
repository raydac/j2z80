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
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.bcel.generic.ClassGen;

public final class InstanceofTable {
  private final List<InstanceOfRow> rows = new ArrayList<>();

  public InstanceofTable(final TranslatorContext translator, final Set<ClassID> classesToBeChecked) {
    final ClassContext classContext = translator.getClassContext();

    for (final ClassID c : classesToBeChecked) {
      final InstanceOfRow row = addRow(c);

      final ClassGen classGen = classContext.findClassForID(c);

      final List<String> allSuccesors = classContext.findAllClassSuccessors(classGen.getClassName());
      final Set<ClassID> allImplementingInterface = classContext.findAllClassesImplementInterface(classGen.getClassName());

      final Set<ClassID> allCompatible = new HashSet<>();
      for (final String s : allSuccesors) {
        allCompatible.add(new ClassID(s));
      }
      allCompatible.addAll(allImplementingInterface);

      row.addClass(c);
      for (final ClassID cc : allCompatible) {
        row.addClass(cc);
      }
    }
  }

  public void clear() {
    rows.clear();
  }

  public int size() {
    return rows.size();
  }

  public InstanceOfRow addRow(final ClassID classId) {
    InstanceOfRow row = new InstanceOfRow(classId);

    final int existRowIndex = rows.indexOf(row);
    if (existRowIndex < 0) {
      rows.add(row);
    } else {
      row = rows.get(existRowIndex);
    }

    return row;
  }

  public String toAsm() {
    final StringBuilder result = new StringBuilder();
    result.append("DEFB ").append(rows.size()).append('\n');
    for (final InstanceOfRow row : rows) {
      result.append(row.toAsm()).append('\n');
    }
    return result.toString();
  }

  public final static class InstanceOfRow {
    private final ClassID classId;
    private final Set<ClassID> compatibleClasses = new HashSet<>();

    public InstanceOfRow(final ClassID classId) {
      this.classId = classId;
    }

    public ClassID getClassID() {
      return this.classId;
    }

    public void addClass(final ClassID classId) {
      compatibleClasses.add(classId);
    }

    public void addClasses(final ClassID... ids) {
      for (final ClassID i : ids) {
        addClass(i);
      }
    }

    public String toAsm() {
      final StringBuilder result = new StringBuilder();
      result.append("DEFW ").append(LabelAndFrameUtils.makeLabelForClassID(this.classId))
          .append("\n");

      result.append("DEFB ").append(compatibleClasses.size()).append("\n");
      result.append("DEFW ");
      boolean first = true;
      for (final ClassID compatibleClass : compatibleClasses) {
        if (!first) {
          result.append(',');
        }
        result.append(LabelAndFrameUtils.makeLabelForClassID(compatibleClass));
        first = false;
      }
      return result.toString();
    }

    @Override
    public int hashCode() {
      return classId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null || obj.getClass() != InstanceOfRow.class) {
        return false;
      }

      final InstanceOfRow thatRow = (InstanceOfRow) obj;

      return this.classId.equals(thatRow.classId);
    }
  }
}
