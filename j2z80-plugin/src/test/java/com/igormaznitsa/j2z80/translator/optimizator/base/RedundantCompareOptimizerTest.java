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

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.j2z80.translator.optimizator.OptimizationChainFactory;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.util.List;
import org.junit.Test;

public class RedundantCompareOptimizerTest {

  private final RedundantCompareOptimizer optimizer = new RedundantCompareOptimizer();

  @Test
  public void removesCompareBeforeInstructionThatOverwritesAllFlags() {
    assertOptimized(List.of("CP 0", "OR A", "CP 0", "CP B", "CP 0", "ADD A,C"),
        List.of("OR A", "CP B", "ADD A,C"));
  }

  @Test
  public void retainsLabelOnRemovedCompare() {
    assertOptimized(List.of("entry: CP 0", "OR A"), List.of("entry:", "OR A"));
  }

  @Test
  public void preservesCompareWhenFlagsRemainLiveOrCarryIsConsumed() {
    assertOptimized(List.of("CP 0", "JP Z,target", "CP 0", "ADC A,B", "CP 0", "INC A"),
        List.of("CP 0", "JP Z,target", "CP 0", "ADC A,B", "CP 0", "INC A"));
  }

  @Test
  public void compactProfileIncludesRedundantCompareOptimization() {
    final List<ParsedAsmLine> input = List.of("CP 0", "AND B").stream()
        .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("AND B")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
