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

package com.igormaznitsa.z80asm;

import com.igormaznitsa.meta.common.utils.Assertions;
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
    Assertions.assertNotNull("Must not be null", associatedLabel);
    return directiveContainer.get(associatedLabel);
  }

  public EquDirectiveRecord addRecord(final String associatedLabel, final ParsedAsmLine parsedAsmLine, final int pcCounter) {
    Assertions.assertNotNull("Label Must not be null", associatedLabel);
    Assertions.assertNotNull("Line must not be null", parsedAsmLine);
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
