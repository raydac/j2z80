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
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

public class ImmediateWordLoadOptimizer implements AsmOptimizer {

  private static final int MINIMUM_BYTE_IMMEDIATE = -128;
  private static final int MAXIMUM_BYTE_IMMEDIATE = 255;
  private static final Map<String, String> WORD_REGISTER_BY_BYTES =
      Map.of("B,C", "BC", "D,E", "DE", "H,L", "HL");

  private static OptionalInt parseByteImmediate(final String value) {
    try {
      final int parsed = value.startsWith("#")
          ? Integer.parseInt(value.substring(1), 16)
          : Integer.parseInt(value);
      return parsed < MINIMUM_BYTE_IMMEDIATE || parsed > MAXIMUM_BYTE_IMMEDIATE
          ? OptionalInt.empty()
          : OptionalInt.of(parsed);
    } catch (final NumberFormatException ex) {
      return OptionalInt.empty();
    }
  }

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context,
                                             final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new ArrayList<>(lines.size());
    for (int index = 0; index < lines.size(); ) {
      final int wordValue = this.combinedImmediateWord(index, lines);
      if (wordValue < 0) {
        result.add(lines.get(index++));
        continue;
      }

      final ParsedAsmLine highByteLoad = lines.get(index);
      final ParsedAsmLine lowByteLoad = lines.get(index + 1);
      final String registerPair =
          WORD_REGISTER_BY_BYTES.get(
              highByteLoad.getArgs().get(0) + ',' + lowByteLoad.getArgs().get(0));
      result.add(new ParsedAsmLine(highByteLoad.getLabel(), "LD", registerPair,
          String.format(Locale.ROOT, "#%04X", wordValue)));
      index += 2;
    }
    return result;
  }

  private int combinedImmediateWord(final int index, final List<ParsedAsmLine> lines) {
    if (index + 1 >= lines.size()) {
      return -1;
    }

    final ParsedAsmLine highByteLoad = lines.get(index);
    final ParsedAsmLine lowByteLoad = lines.get(index + 1);
    final List<String> highArguments = highByteLoad.getArgs();
    final List<String> lowArguments = lowByteLoad.getArgs();
    if (!"LD".equals(highByteLoad.getCommand()) || !"LD".equals(lowByteLoad.getCommand())
        || lowByteLoad.getLabel() != null || highArguments.size() != 2 || lowArguments.size() != 2
        || !WORD_REGISTER_BY_BYTES.containsKey(highArguments.get(0) + ',' + lowArguments.get(0))) {
      return -1;
    }

    final OptionalInt highByte = parseByteImmediate(highArguments.get(1));
    final OptionalInt lowByte = parseByteImmediate(lowArguments.get(1));
    if (highByte.isEmpty() || lowByte.isEmpty()) {
      return -1;
    }
    return ((highByte.getAsInt() & 0xFF) << 8) | (lowByte.getAsInt() & 0xFF);
  }
}
