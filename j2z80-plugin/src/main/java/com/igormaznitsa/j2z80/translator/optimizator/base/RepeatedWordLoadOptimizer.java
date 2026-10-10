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

public class RepeatedWordLoadOptimizer implements AsmOptimizer {

  private static final Set<String> LOADABLE_WORD_REGISTERS = Set.of("BC", "DE", "HL", "IX", "IY");

  private static String pushedRegister(final int index, final List<ParsedAsmLine> lines) {
    if (index + 2 >= lines.size()) {
      return null;
    }
    final ParsedAsmLine push = lines.get(index);
    final String register = push.getSignature();
    return LOADABLE_WORD_REGISTERS.contains(register) && isPush(push, register)
        && isPush(lines.get(index + 2), register) ? register : null;
  }

  private static boolean isRepeatedLoadFollowedByPush(final ParsedAsmLine load,
                                                      final String register,
                                                      final String loadedValue,
                                                      final List<ParsedAsmLine> lines,
                                                      final int index) {
    if (load.getLabel() != null || !isLoad(load, register)
        || !loadedValue.equals(load.getArgs().get(1)) || index + 1 >= lines.size()) {
      return false;
    }
    final ParsedAsmLine followingPush = lines.get(index + 1);
    return followingPush.getLabel() == null && isPush(followingPush, register);
  }

  private static boolean isLoad(final ParsedAsmLine line, final String register) {
    return "LD".equals(line.getCommand()) && line.getArgs().size() == 2
        && register.equals(line.getArgs().getFirst());
  }

  private static boolean isPush(final ParsedAsmLine line, final String register) {
    return "PUSH".equals(line.getCommand()) && register.equals(line.getSignature());
  }

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); ) {
      final String register = pushedRegister(index, lines);
      if (register == null || !isLoad(lines.get(index + 1), register)) {
        result.add(lines.get(index++));
        continue;
      }

      final List<String> loadArguments = lines.get(index + 1).getArgs();
      final String loadedValue = loadArguments.get(1);
      result.add(lines.get(index++));
      result.add(lines.get(index++));
      result.add(lines.get(index++));

      while (index < lines.size()) {
        final ParsedAsmLine current = lines.get(index);
        if (isPush(current, register)) {
          result.add(current);
          index++;
          if (current.getLabel() != null) {
            break;
          }
        } else if (isRepeatedLoadFollowedByPush(current, register, loadedValue, lines, index)) {
          result.add(lines.get(index + 1));
          index += 2;
        } else {
          break;
        }
      }
    }
    return result;
  }
}
