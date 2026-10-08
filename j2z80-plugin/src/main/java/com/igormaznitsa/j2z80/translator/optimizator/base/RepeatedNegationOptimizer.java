/*
 * Copyright 2012-2026 Igor Maznitsa.
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

package com.igormaznitsa.j2z80.translator.optimizator.base;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.translator.optimizator.AsmOptimizer;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.util.ArrayList;
import java.util.List;

public class RepeatedNegationOptimizer implements AsmOptimizer {

  private static final String HELPER_LABEL = "___COMPACT_NEGATE_BC";
  private static final int SEQUENCE_LENGTH = 7;
  private static final int MINIMUM_OCCURRENCES = 3;
  private static final List<String> COMMANDS = List.of(
      "LD", "CPL", "LD", "LD", "CPL", "LD", "INC"
  );
  private static final List<String> SIGNATURES = List.of(
      "A,B", "", "B,A", "A,C", "", "C,A", "BC"
  );

  private static boolean containsHelper(final List<ParsedAsmLine> lines) {
    return lines.stream().anyMatch(line -> HELPER_LABEL.equals(line.getLabel()));
  }

  private static List<Integer> findSequenceStarts(final List<ParsedAsmLine> lines) {
    final List<Integer> starts = new ArrayList<>();
    for (int index = 0; index <= lines.size() - SEQUENCE_LENGTH; ) {
      if (matchesSequence(index, lines)) {
        starts.add(index);
        index += SEQUENCE_LENGTH;
      } else {
        index++;
      }
    }
    return starts;
  }

  private static boolean matchesSequence(final int index, final List<ParsedAsmLine> lines) {
    for (int offset = 0; offset < SEQUENCE_LENGTH; offset++) {
      final ParsedAsmLine line = lines.get(index + offset);
      if (offset > 0 && line.getLabel() != null) {
        return false;
      }
      if (!COMMANDS.get(offset).equals(line.getCommand())
          || !SIGNATURES.get(offset).equals(line.getSignature())) {
        return false;
      }
    }
    return true;
  }

  private static void insertHelper(final List<ParsedAsmLine> lines) {
    final int managerPointerIndex = findManagerPointerIndex(lines);
    final int insertionIndex = managerPointerIndex < 0 ? lines.size() : managerPointerIndex;
    final List<ParsedAsmLine> helper = List.of(
        new ParsedAsmLine(HELPER_LABEL, "LD", "A", "B"),
        new ParsedAsmLine("CPL"),
        new ParsedAsmLine(null, "LD", "B", "A"),
        new ParsedAsmLine(null, "LD", "A", "C"),
        new ParsedAsmLine("CPL"),
        new ParsedAsmLine(null, "LD", "C", "A"),
        new ParsedAsmLine(null, "INC", "BC"),
        new ParsedAsmLine("RET"));
    lines.addAll(insertionIndex, helper);
  }

  private static int findManagerPointerIndex(final List<ParsedAsmLine> lines) {
    for (int index = 0; index < lines.size(); index++) {
      if ("___MEMORY_MANAGER_TOP_POINTER".equals(lines.get(index).getLabel())) {
        return index;
      }
    }
    return -1;
  }

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    if (containsHelper(lines)) {
      return lines;
    }

    final List<Integer> starts = findSequenceStarts(lines);
    if (starts.size() < MINIMUM_OCCURRENCES) {
      return lines;
    }

    final List<ParsedAsmLine> optimized = new ArrayList<>(lines.size() + SEQUENCE_LENGTH + 1);
    int nextStartIndex = 0;
    for (int index = 0; index < lines.size(); ) {
      if (nextStartIndex < starts.size() && index == starts.get(nextStartIndex)) {
        final ParsedAsmLine first = lines.get(index);
        optimized.add(new ParsedAsmLine(first.getLabel(), "CALL", HELPER_LABEL));
        index += SEQUENCE_LENGTH;
        nextStartIndex++;
      } else {
        optimized.add(lines.get(index++));
      }
    }
    insertHelper(optimized);
    return optimized;
  }
}
