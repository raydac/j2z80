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

public class RedundantAccumulatorOptimizerTest {

  private final RedundantAccumulatorOptimizer optimizer = new RedundantAccumulatorOptimizer();

  @Test
  public void combinesZeroLoadWithXorOrToSingleFlagSettingClear() {
    assertOptimized(List.of("LD A,0", "XOR A", "LD A,0", "OR A"),
        List.of("XOR A", "XOR A"));
  }

  @Test
  public void removesDuplicateIdempotentAccumulatorOperations() {
    assertOptimized(List.of("XOR A", "XOR A", "OR A", "OR A", "AND A", "AND A",
            "LD A,0", "LD A,0"),
        List.of("XOR A", "OR A", "AND A", "LD A,0"));
  }

  @Test
  public void preservesLabelsOnTheFirstOperationAndDoesNotCrossSecondLabel() {
    assertOptimized(List.of("clear: LD A,0", "XOR A", "XOR A", "again: XOR A", "XOR A"),
        List.of("clear: XOR A", "XOR A", "again: XOR A"));
  }

  @Test
  public void compactProfileRepeatsPassesToRemoveNewlyExposedDuplicates() {
    final List<ParsedAsmLine> input = List.of("LD A,0", "XOR A").stream()
        .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("XOR A")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
