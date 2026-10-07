/*
 * Copyright 2012-2026 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

public class RedundantAccumulatorOptimizer implements AsmOptimizer {

  private static final Set<String> IDEMPOTENT_OPERATIONS = Set.of("LD", "XOR", "OR", "AND");

  private static boolean isAccumulatorOperation(final ParsedAsmLine line,
                                                final String command) {
    return command.equals(line.getCommand()) && "A".equals(line.getSignature());
  }

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); ) {
      final ParsedAsmLine first = lines.get(index);
      if (index + 1 < lines.size() && this.canCollapseZeroAccumulatorPair(first,
          lines.get(index + 1))) {
        result.add(new ParsedAsmLine(first.getLabel(), "XOR", "A"));
        index += 2;
      } else if (index + 1 < lines.size()
          && this.isDuplicateIdempotentOperation(first, lines.get(index + 1))) {
        result.add(first);
        index += 2;
      } else {
        result.add(first);
        index++;
      }
    }
    return result;
  }

  private boolean canCollapseZeroAccumulatorPair(final ParsedAsmLine first,
                                                 final ParsedAsmLine second) {
    if (!"LD".equals(first.getCommand()) || second.getLabel() != null) {
      return false;
    }

    final String[] firstArguments = first.getArgs();
    final String[] secondArguments = second.getArgs();
    return firstArguments.length == 2 && "A".equals(firstArguments[0])
        && "0".equals(firstArguments[1])
        && (isAccumulatorOperation(second, "XOR") || isAccumulatorOperation(second, "OR"))
        && secondArguments.length == 1;
  }

  private boolean isDuplicateIdempotentOperation(final ParsedAsmLine first,
                                                 final ParsedAsmLine second) {
    if (second.getLabel() != null || !IDEMPOTENT_OPERATIONS.contains(first.getCommand())
        || !first.getCommand().equals(second.getCommand())
        || !first.getSignature().equals(second.getSignature())) {
      return false;
    }

    return switch (first.getCommand()) {
      case "XOR" -> first.getSignature().equals("A");
      case "OR", "AND" -> first.getSignature().equals("A");
      case "LD" -> first.getSignature().equals("A,0");
      default -> false;
    };
  }
}
