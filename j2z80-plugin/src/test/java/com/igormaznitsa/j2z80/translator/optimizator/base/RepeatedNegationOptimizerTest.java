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
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.j2z80.translator.optimizator.OptimizationChainFactory;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class RepeatedNegationOptimizerTest {

  private final RepeatedNegationOptimizer optimizer = new RepeatedNegationOptimizer();

  private static void assertHelperBeforeMemoryManager(final List<ParsedAsmLine> lines) {
    final int helperIndex = findLabel(lines, "___COMPACT_NEGATE_BC");
    final int managerPointerIndex = findLabel(lines, "___MEMORY_MANAGER_TOP_POINTER");
    if (managerPointerIndex >= 0) {
      assertTrue(helperIndex < managerPointerIndex);
      assertEquals("RET", lines.get(helperIndex + 7).getCommand());
    } else {
      assertEquals("___COMPACT_NEGATE_BC", lines.get(lines.size() - 8).getLabel());
      assertEquals("RET", lines.getLast().getCommand());
    }
  }

  private static int findLabel(final List<ParsedAsmLine> lines, final String label) {
    for (int index = 0; index < lines.size(); index++) {
      if (label.equals(lines.get(index).getLabel())) {
        return index;
      }
    }
    return -1;
  }

  private static List<ParsedAsmLine> repeatedNegations(final int count) {
    final List<ParsedAsmLine> result = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      result.addAll(repeatedNegation(null));
    }
    return result;
  }

  private static List<ParsedAsmLine> repeatedNegation(final String firstLabel) {
    return repeatedNegation(firstLabel, null);
  }

  private static List<ParsedAsmLine> repeatedNegation(final String firstLabel,
                                                      final String interiorLabel) {
    return List.of(
        new ParsedAsmLine(firstLabel, "LD", "A", "B"),
        new ParsedAsmLine(null, "CPL"),
        new ParsedAsmLine(null, "LD", "B", "A"),
        new ParsedAsmLine(null, "LD", "A", "C"),
        new ParsedAsmLine(null, "CPL"),
        new ParsedAsmLine(null, "LD", "C", "A"),
        new ParsedAsmLine(interiorLabel, "INC", "BC"));
  }

  @Test
  public void extractsOnlyWhenTheHelperAmortizesItsCallAndBodySize() {
    final List<ParsedAsmLine> twoOccurrences = repeatedNegations(2);
    assertEquals(twoOccurrences, this.optimizer.optimizeAsmText(null, twoOccurrences));

    final List<ParsedAsmLine> threeOccurrences =
        this.optimizer.optimizeAsmText(null, repeatedNegations(3));
    assertEquals(3, threeOccurrences.stream()
        .filter(line -> "CALL".equals(line.getCommand())
            && "___COMPACT_NEGATE_BC".equals(line.getSignature()))
        .count());
    assertHelperBeforeMemoryManager(threeOccurrences);
  }

  @Test
  public void keepsEntryLabelsAndDoesNotReplaceSequencesWithInteriorLabels() {
    final List<ParsedAsmLine> input = new ArrayList<>();
    input.addAll(repeatedNegation("entry"));
    input.addAll(repeatedNegation("unsafe", "target"));
    input.addAll(repeatedNegation(null));
    input.addAll(repeatedNegation(null));
    final List<ParsedAsmLine> result = this.optimizer.optimizeAsmText(null, input);

    assertEquals("entry", result.get(0).getLabel());
    assertEquals("CALL", result.get(0).getCommand());
    assertEquals("unsafe", result.get(1).getLabel());
    assertEquals("INC", result.get(7).getCommand());
    assertEquals("target", result.get(7).getLabel());
    assertEquals(3, result.stream()
        .filter(line -> "CALL".equals(line.getCommand())
            && "___COMPACT_NEGATE_BC".equals(line.getSignature()))
        .count());
  }

  @Test
  public void leavesHelperAfterMemoryManagerCodeAndBeforeHeapStartData() {
    final List<ParsedAsmLine> input = repeatedNegations(3);
    input.add(new ParsedAsmLine("___MEMORY_MANAGER_TOP_POINTER:"));
    input.add(new ParsedAsmLine("DEFW ___MEMORY_HEAP_START_AREA"));
    input.add(new ParsedAsmLine("___MEMORY_HEAP_START_AREA: EQU $"));

    final List<ParsedAsmLine> result = this.optimizer.optimizeAsmText(null, input);
    assertHelperBeforeMemoryManager(result);
    assertEquals("___MEMORY_MANAGER_TOP_POINTER",
        result.get(result.size() - 3).getLabel());
    assertEquals("___MEMORY_HEAP_START_AREA", result.getLast().getLabel());
  }

  @Test
  public void isStableWhenTheOptimizerChainRunsAgainAndIsWiredIntoCompact() {
    final List<ParsedAsmLine> input = repeatedNegations(3);
    final List<ParsedAsmLine> optimized = this.optimizer.optimizeAsmText(null, input);
    assertEquals(optimized, this.optimizer.optimizeAsmText(null, optimized));

    final List<ParsedAsmLine> compact =
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(repeatedNegations(3));
    assertEquals(3, compact.stream()
        .filter(line -> "CALL".equals(line.getCommand())
            && "___COMPACT_NEGATE_BC".equals(line.getSignature()))
        .count());
  }
}
