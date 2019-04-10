/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.igormaznitsa.j2z80.translator.optimizator.base;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.translator.optimizator.AsmOptimizer;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.LinkedList;
import java.util.List;

public class ReplacePatterns implements AsmOptimizer {

  @Override
  public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context, final List<ParsedAsmLine> lines) {
    final List<ParsedAsmLine> result = new LinkedList<>(lines);

    boolean loop = true;

    while (loop && !Thread.currentThread().isInterrupted()) {
      loop = false;
      for (final OptimizationState optCase : OptimizationState.values()) {
        if (optCase.process(result)) {
          loop = true;
          break;
        }
      }
    }

    return result;
  }

}
