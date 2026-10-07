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

public class FallThroughJumpOptimizerTest {

  private final FallThroughJumpOptimizer optimizer = new FallThroughJumpOptimizer();

  @Test
  public void removesUnconditionalAndConditionalJumpsToNextLabel() {
    assertOptimized(
        List.of("JP next", "next:", "JR jump", "jump:", "JP NZ,done", "done:", "JR C,last",
            "last: RET"),
        List.of("next:", "jump:", "done:", "last: RET"));
  }

  @Test
  public void preservesSourceLabelWhenRemovingJump() {
    assertOptimized(List.of("entry: JP next", "next: RET"), List.of("entry:", "next: RET"));
  }

  @Test
  public void preservesJumpsWhenTargetIsNotTheNextLabel() {
    assertOptimized(List.of("JP later", "middle: NOP", "later: RET"),
        List.of("JP later", "middle: NOP", "later: RET"));
  }

  @Test
  public void preservesJumpsWhenTargetIsSeparatedByAnotherDirective() {
    assertOptimized(List.of("JP next", "CLRLOC", "next: RET"),
        List.of("JP next", "CLRLOC", "next: RET"));
  }

  @Test
  public void preservesJumpsToEquatesAndOrigins() {
    assertOptimized(List.of("JP target", "target: EQU #1234", "ORG #8000"),
        List.of("JP target", "target: EQU #1234", "ORG #8000"));
  }

  @Test
  public void compactProfileIncludesBasicStackCleanup() {
    final List<ParsedAsmLine> input =
        List.of("PUSH HL", "POP HL", "JP next", "next: RET").stream()
            .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("next: RET")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  @Test
  public void compactProfileRepeatsPassesAfterRemovingInstructions() {
    final List<ParsedAsmLine> input =
        List.of("JP target", "CP 0", "target: OR A").stream()
            .map(ParsedAsmLine::new).toList();
    assertEquals(List.of(new ParsedAsmLine("target: OR A")),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
