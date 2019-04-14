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
