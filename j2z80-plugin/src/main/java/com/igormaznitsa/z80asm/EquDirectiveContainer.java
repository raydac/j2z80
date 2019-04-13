/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The class implements an inside container to save met equ directives to process them later.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class EquDirectiveContainer {
  private final Map<String, EquDirectiveRecord> directiveContainer = new LinkedHashMap<>();

  public EquDirectiveRecord findRecordForLabel(final String associatedLabel) {
    Assert.assertNotNull("Must not be null", associatedLabel);
    return directiveContainer.get(associatedLabel);
  }

  public EquDirectiveRecord addRecord(final String associatedLabel, final ParsedAsmLine parsedAsmLine, final int pcCounter) {
    Assert.assertNotNull("Must not be null", associatedLabel, parsedAsmLine);
    return directiveContainer.put(associatedLabel, new EquDirectiveRecord(associatedLabel, parsedAsmLine, pcCounter));
  }

  public List<EquDirectiveRecord> getValuesAsList() {
    final List<EquDirectiveRecord> result = new ArrayList<>(directiveContainer.size());
    for (final String key : directiveContainer.keySet()) {
      result.add(directiveContainer.get(key));
    }
    return result;
  }

  public void clear() {
    directiveContainer.clear();
  }

  public static class EquDirectiveRecord {

    private final int pcCounterState;
    private final ParsedAsmLine parsedAsmLine;
    private final String associatedLabel;

    public EquDirectiveRecord(final String associatedLabel, final ParsedAsmLine parsedAsmString, final int pcCounter) {
      this.pcCounterState = pcCounter;
      this.parsedAsmLine = parsedAsmString;
      this.associatedLabel = associatedLabel;
    }

    public int getPC() {
      return this.pcCounterState;
    }

    public String getAssociatedLabel() {
      return this.associatedLabel;
    }

    public ParsedAsmLine getParsedAsmLine() {
      return this.parsedAsmLine;
    }
  }
}
