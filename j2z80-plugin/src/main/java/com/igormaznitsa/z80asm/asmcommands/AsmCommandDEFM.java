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

package com.igormaznitsa.z80asm.asmcommands;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.z80asm.AsmTranslator;

public class AsmCommandDEFM extends AbstractAsmCommand {

  private static String unescape(final String str) {
    if (str.isEmpty()) {
      return str;
    }

    requireNonNull(str, "String must not be null");
    if (!str.startsWith("\"")) {
      throw new IllegalArgumentException("DEFM takes a string as argument [" + str + ']');
    }
    if (!str.endsWith("\"")) {
      throw new IllegalArgumentException("String must be closed [" + str + ']');
    }

    return str.substring(1, str.length() - 1);
  }

  private static byte[] toByteValues(final String text) {
    final byte[] bytes = new byte[text.length()];
    for (int index = 0; index < text.length(); index++) {
      final char chr = text.charAt(index);
      if (chr > 0xFF) {
        throw new IllegalArgumentException("DEFM character does not fit in a byte [" + text + ']');
      }
      bytes[index] = (byte) chr;
    }
    return bytes;
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    return toByteValues(unescape(asm.getArgs()[0]));
  }

  @Override
  public String getName() {
    return "DEFM";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ONE;
  }

  @Override
  public boolean isSpecialDirective() {
    return true;
  }

}
