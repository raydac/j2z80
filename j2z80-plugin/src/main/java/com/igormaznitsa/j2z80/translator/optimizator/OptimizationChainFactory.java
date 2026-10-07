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

package com.igormaznitsa.j2z80.translator.optimizator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.translator.optimizator.base.FallThroughJumpOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.ImmediateWordLoadOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.NegationOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.RedundantAccumulatorOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.RedundantCompareOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.RepeatedNegationOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.RepeatedWordLoadOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.ReplacePatterns;
import com.igormaznitsa.j2z80.translator.optimizator.base.WordShiftOptimizer;
import com.igormaznitsa.j2z80.translator.optimizator.base.ZeroLoadOptimizer;

public class OptimizationChainFactory {

  public static AsmOptimizerChain getOptimizations(final TranslatorContext context,
                                                   final OptimizationLevel level) {
    if (level == null) {
      return new AsmOptimizerChain(context);
    }
    return switch (level) {
      case NONE -> new AsmOptimizerChain(context);
      case BASIC -> new AsmOptimizerChain(context, new ReplacePatterns());
      case COMPACT ->
          new AsmOptimizerChain(context, new ReplacePatterns(), new FallThroughJumpOptimizer(),
              new ZeroLoadOptimizer(), new RedundantCompareOptimizer(), new NegationOptimizer(),
              new WordShiftOptimizer(), new ImmediateWordLoadOptimizer(),
              new RedundantAccumulatorOptimizer(), new RepeatedWordLoadOptimizer(),
              new RepeatedNegationOptimizer());
    };
  }
}
