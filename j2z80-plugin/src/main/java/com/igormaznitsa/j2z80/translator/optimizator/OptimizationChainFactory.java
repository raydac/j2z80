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
import com.igormaznitsa.j2z80.translator.optimizator.base.ReplacePatterns;

public class OptimizationChainFactory {

  public static AsmOptimizerChain getOptimizators(final TranslatorContext context, final OptimizationLevel level) {
    if (level == null) {
      return new AsmOptimizerChain(context);
    }
    switch (level) {
      case NONE:
        return new AsmOptimizerChain(context);
      case BASIC:
        return new AsmOptimizerChain(context, new ReplacePatterns());
      default:
        throw new IllegalArgumentException("Unsupported optimization level " + level);
    }
  }
}
