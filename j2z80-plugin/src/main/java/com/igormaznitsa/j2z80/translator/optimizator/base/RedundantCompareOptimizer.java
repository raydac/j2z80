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

public class RedundantCompareOptimizer implements AsmOptimizer {

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); index++) {
      final ParsedAsmLine line = lines.get(index);
      if (this.isRedundantCompare(line, index, lines)) {
        if (line.getLabel() != null) {
          result.add(new ParsedAsmLine(line.getLabel(), "", new String[0]));
        }
      } else {
        result.add(line);
      }
    }
    return result;
  }

  private boolean isRedundantCompare(final ParsedAsmLine line, final int index,
                                     final List<ParsedAsmLine> lines) {
    if (!"CP".equals(line.getCommand()) || index + 1 >= lines.size()) {
      return false;
    }

    final List<String> arguments = line.getArgs();
    return arguments.size() == 1 && "0".equals(arguments.get(0))
        && Z80FlagUtils.overwritesAllFlags(lines.get(index + 1));
  }
}
