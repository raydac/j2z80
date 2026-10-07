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

public class WordShiftOptimizer implements AsmOptimizer {

  private static final int SHIFTS_PER_BYTE = 8;

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); ) {
      final int runLength = this.countUnlabeledShifts(index, lines);
      final int optimizedGroups = this.countReplaceableGroups(index, runLength, lines);
      if (optimizedGroups == 0) {
        result.add(lines.get(index++));
        continue;
      }

      final ParsedAsmLine firstShift = lines.get(index);
      result.add(new ParsedAsmLine(firstShift.getLabel(), "LD", "H", "L"));
      result.add(new ParsedAsmLine("LD L,0"));
      index += SHIFTS_PER_BYTE;
      for (int group = 1; group < optimizedGroups; group++) {
        result.add(new ParsedAsmLine("LD H,L"));
        result.add(new ParsedAsmLine("LD L,0"));
        index += SHIFTS_PER_BYTE;
      }
    }
    return result;
  }

  private int countUnlabeledShifts(final int start, final List<ParsedAsmLine> lines) {
    int index = start;
    while (index < lines.size() && this.isShiftLeft(lines.get(index))
        && (index == start || lines.get(index).getLabel() == null)) {
      index++;
    }
    return index - start;
  }

  private int countReplaceableGroups(final int start, final int runLength,
                                     final List<ParsedAsmLine> lines) {
    int groups = runLength / SHIFTS_PER_BYTE;
    if (groups == 0) {
      return 0;
    }

    if (runLength % SHIFTS_PER_BYTE == 0
        && (start + runLength == lines.size()
        || !Z80FlagUtils.overwritesAllFlags(lines.get(start + runLength)))) {
      groups--;
    }
    return groups;
  }

  private boolean isShiftLeft(final ParsedAsmLine line) {
    return "ADD".equals(line.getCommand()) && "HL,HL".equals(line.getSignature());
  }
}
