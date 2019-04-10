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

package com.igormaznitsa.j2z80.translator.optimizator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.ArrayList;
import java.util.List;

/**
 * It is a container allows to make a chain from several assembler optimizers.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class AsmOptimizerChain {
  /**
   * The Translator context for the optimizer chain
   */
  private final TranslatorContext context;

  /**
   * Array of optimizers of the chain
   */
  private final AsmOptimizer[] optimizators;

  /**
   * The Constructor.
   *
   * @param context      the translator context, must not be null
   * @param optimizators an array of optimizers for the chain, must not be null
   */
  protected AsmOptimizerChain(final TranslatorContext context, final AsmOptimizer... optimizators) {
    this.context = context;
    this.optimizators = optimizators == null ? new AsmOptimizer[0] : optimizators.clone();
  }

  /**
   * Process assembler sources by the chain
   *
   * @param lines a list contains parsed assembler lines, also it doesn't contain empty strings, must not be null
   * @return an optimized list of assembler strings
   */
  public List<ParsedAsmLine> processSources(final List<ParsedAsmLine> lines) {
    List<ParsedAsmLine> processing = new ArrayList<ParsedAsmLine>(lines);
    for (final AsmOptimizer optimizator : optimizators) {
      processing = optimizator.optimizeAsmText(this.context, processing);
    }
    return processing;
  }
}
