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

public class FallThroughJumpOptimizer implements AsmOptimizer {

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); index++) {
      final ParsedAsmLine line = lines.get(index);
      if (index + 1 < lines.size() && this.targetsNextLine(line, lines.get(index + 1))) {
        if (line.getLabel() != null) {
          result.add(new ParsedAsmLine(line.getLabel(), "", new String[0]));
        }
      } else {
        result.add(line);
      }
    }
    return result;
  }

  private boolean targetsNextLine(final ParsedAsmLine line, final ParsedAsmLine nextLine) {
    if (!"JP".equals(line.getCommand()) && !"JR".equals(line.getCommand())) {
      return false;
    }

    if ("EQU".equals(nextLine.getCommand()) || "ORG".equals(nextLine.getCommand())
        || "END".equals(nextLine.getCommand())) {
      return false;
    }

    final String[] arguments = line.getArgs();
    if (arguments.length == 1) {
      return arguments[0].equals(nextLine.getLabel());
    }
    if (arguments.length == 2) {
      return arguments[1].equals(nextLine.getLabel());
    }
    return false;
  }
}
