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

public class WordShiftOptimizerTest {

  private final WordShiftOptimizer optimizer = new WordShiftOptimizer();

  @Test
  public void replacesEightDoublingsWhenFlagsAreOverwritten() {
    assertOptimized(List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "OR A"),
        List.of("LD H,L", "LD L,0", "OR A"));
  }

  @Test
  public void preservesFinalDoublingWhenItsFlagsRemainLive() {
    assertOptimized(List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "JP C,target"),
        List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "JP C,target"));
  }

  @Test
  public void keepsOneDoublingToProduceEquivalentFinalFlags() {
    assertOptimized(List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "JP C,target"),
        List.of("LD H,L", "LD L,0", "ADD HL,HL", "JP C,target"));
  }

  @Test
  public void preservesFlagsBeforeLabeledShifts() {
    assertOptimized(List.of("entry: ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "target: ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "OR A"),
        List.of("entry: ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "target: ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "OR A"));
  }

  @Test
  public void preservesEntryLabelOnReplacement() {
    assertOptimized(List.of("entry: ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
            "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "OR A"),
        List.of("entry: LD H,L", "LD L,0", "OR A"));
  }

  @Test
  public void preservesRunsShorterThanEight() {
    assertOptimized(List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL"),
        List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL"));
  }

  @Test
  public void compactProfileIncludesWordShiftOptimization() {
    final List<ParsedAsmLine> input =
        List.of("ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL", "ADD HL,HL",
                "ADD HL,HL", "ADD HL,HL", "XOR A").stream()
            .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("LD H,L"), new ParsedAsmLine("LD L,0"),
            new ParsedAsmLine("XOR A")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
