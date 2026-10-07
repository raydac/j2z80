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

public class NegationOptimizerTest {

  private final NegationOptimizer optimizer = new NegationOptimizer();

  @Test
  public void replacesLoadThenNegateWithClearAndSubtract() {
    assertOptimized(List.of("LD A,B", "NEG", "LD A,L", "NEG"),
        List.of("XOR A", "SUB B", "XOR A", "SUB L"));
  }

  @Test
  public void preservesLoadLabelAndDoesNotRemoveNegationLabel() {
    assertOptimized(List.of("entry: LD A,C", "NEG", "negate: NEG"),
        List.of("entry: XOR A", "SUB C", "negate: NEG"));
  }

  @Test
  public void preservesOtherLoadsAndLabeledNegations() {
    assertOptimized(List.of("LD A,A", "NEG", "LD B,A", "NEG", "LD A,(HL)", "NEG", "LD A,D",
            "negate: NEG"),
        List.of("LD A,A", "NEG", "LD B,A", "NEG", "LD A,(HL)", "NEG", "LD A,D", "negate: NEG"));
  }

  @Test
  public void compactProfileIncludesNegationOptimization() {
    final List<ParsedAsmLine> input = List.of("LD A,B", "NEG").stream()
        .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("XOR A"), new ParsedAsmLine("SUB B")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
