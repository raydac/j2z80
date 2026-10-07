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

public class ZeroLoadOptimizerTest {

  private final ZeroLoadOptimizer optimizer = new ZeroLoadOptimizer();

  @Test
  public void usesXorToClearAccumulatorBeforeFullFlagWriters() {
    assertOptimized(
        List.of("LD A,0", "AND B", "LD A,0", "OR A", "LD A,0", "XOR C", "LD A,0", "CP #12",
            "LD A,0", "SUB B", "LD A,0", "ADD A,B"),
        List.of("XOR A", "AND B", "XOR A", "OR A", "XOR A", "XOR C", "XOR A", "CP #12",
            "XOR A", "SUB B", "XOR A", "ADD A,B"));
  }

  @Test
  public void preservesLabelsOnReplacedLoads() {
    assertOptimized(List.of("clear: LD A,0", "check: CP #10"),
        List.of("clear: XOR A", "check: CP #10"));
  }

  @Test
  public void preservesZeroLoadsWhenFlagsCouldBeObservedOrConsumed() {
    assertOptimized(List.of("LD A,0", "ADC A,B", "LD A,0", "INC A", "LD A,0", "LD B,A",
            "LD A,0", "JP Z,target", "target: RET"),
        List.of("LD A,0", "ADC A,B", "LD A,0", "INC A", "LD A,0", "LD B,A",
            "LD A,0", "JP Z,target", "target: RET"));
  }

  @Test
  public void compactProfileIncludesZeroLoadOptimization() {
    final List<ParsedAsmLine> input = List.of("LD A,0", "CP 0").stream()
        .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("XOR A"), new ParsedAsmLine("CP 0")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
