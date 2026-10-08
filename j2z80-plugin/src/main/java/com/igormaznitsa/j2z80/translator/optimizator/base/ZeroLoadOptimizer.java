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

public class ZeroLoadOptimizer implements AsmOptimizer {

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); index++) {
      final ParsedAsmLine line = lines.get(index);
      if (this.canReplaceZeroLoad(line, index, lines)) {
        result.add(new ParsedAsmLine(line.getLabel(), "XOR", "A"));
      } else {
        result.add(line);
      }
    }
    return result;
  }

  private boolean canReplaceZeroLoad(final ParsedAsmLine line, final int index,
                                     final List<ParsedAsmLine> lines) {
    if (!"LD".equals(line.getCommand()) || index + 1 >= lines.size()) {
      return false;
    }

    final List<String> arguments = line.getArgs();
    if (arguments.size() != 2 || !"A".equals(arguments.get(0)) || !"0".equals(arguments.get(1))) {
      return false;
    }

    return Z80FlagUtils.overwritesAllFlags(lines.get(index + 1));
  }
}
