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
import java.util.Set;

public class NegationOptimizer implements AsmOptimizer {

  private static final Set<String> SOURCE_REGISTERS = Set.of("B", "C", "D", "E", "H", "L");

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); index++) {
      if (this.isLoadThenNegate(index, lines)) {
        final ParsedAsmLine load = lines.get(index);
        final String sourceRegister = load.getArgs().get(1);
        result.add(new ParsedAsmLine(load.getLabel(), "XOR", "A"));
        result.add(new ParsedAsmLine("SUB " + sourceRegister));
        index++;
      } else {
        result.add(lines.get(index));
      }
    }
    return result;
  }

  private boolean isLoadThenNegate(final int index, final List<ParsedAsmLine> lines) {
    if (index + 1 >= lines.size()) {
      return false;
    }

    final ParsedAsmLine load = lines.get(index);
    final ParsedAsmLine negate = lines.get(index + 1);
    if (!"LD".equals(load.getCommand()) || !"NEG".equals(negate.getCommand())
        || negate.getLabel() != null) {
      return false;
    }

    final List<String> arguments = load.getArgs();
    return arguments.size() == 2 && "A".equals(arguments.get(0))
        && SOURCE_REGISTERS.contains(arguments.get(1));
  }
}
