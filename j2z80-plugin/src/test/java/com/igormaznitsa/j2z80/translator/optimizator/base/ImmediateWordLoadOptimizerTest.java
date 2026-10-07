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

public class ImmediateWordLoadOptimizerTest {

  private final ImmediateWordLoadOptimizer optimizer = new ImmediateWordLoadOptimizer();

  @Test
  public void combinesImmediateLoadsIntoAllStandardWordPairs() {
    assertOptimized(List.of("LD B,1", "LD C,2", "LD D,#80", "LD E,0", "LD H,-1", "LD L,#FF"),
        List.of("LD BC,#0102", "LD DE,#8000", "LD HL,#FFFF"));
  }

  @Test
  public void preservesFirstLabelOnCombinedInstruction() {
    assertOptimized(List.of("entry: LD D,0", "LD E,1"), List.of("entry: LD DE,#0001"));
  }

  @Test
  public void doesNotCrossLabelsOrCombineRegisterLoads() {
    assertOptimized(List.of("LD D,0", "low: LD E,1", "LD B,A", "LD C,0"),
        List.of("LD D,0", "low: LD E,1", "LD B,A", "LD C,0"));
  }

  @Test
  public void doesNotCombineExpressionsOrOutOfRangeByteValues() {
    assertOptimized(List.of("LD D,VALUE", "LD E,0", "LD D,256", "LD E,0", "LD D,0", "LD E,-129"),
        List.of("LD D,VALUE", "LD E,0", "LD D,256", "LD E,0", "LD D,0", "LD E,-129"));
  }

  @Test
  public void compactProfileIncludesImmediateWordLoadOptimization() {
    final List<ParsedAsmLine> input = List.of("LD D,#80", "LD E,0").stream()
        .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("LD DE,#8000")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
