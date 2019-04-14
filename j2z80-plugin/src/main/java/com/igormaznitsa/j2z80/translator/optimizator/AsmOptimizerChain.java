/* 
 * Copyright 2019 Igor Maznitsa.
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
    List<ParsedAsmLine> processing = new ArrayList<>(lines);
    for (final AsmOptimizer optimizator : optimizators) {
      processing = optimizator.optimizeAsmText(this.context, processing);
    }
    return processing;
  }
}
