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

public class RepeatedWordLoadOptimizerTest {

  private final RepeatedWordLoadOptimizer optimizer = new RepeatedWordLoadOptimizer();

  @Test
  public void removesRepeatedLoadsForStandardAndIndexRegisterPairs() {
    assertOptimized(List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "LD BC,VALUE", "PUSH BC",
            "PUSH DE", "LD DE,#1234", "PUSH DE", "LD DE,#1234", "PUSH DE",
            "PUSH HL", "LD HL,VALUE", "PUSH HL", "LD HL,VALUE", "PUSH HL",
            "PUSH IX", "LD IX,VALUE", "PUSH IX", "LD IX,VALUE", "PUSH IX",
            "PUSH IY", "LD IY,VALUE", "PUSH IY", "LD IY,VALUE", "PUSH IY"),
        List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "PUSH BC",
            "PUSH DE", "LD DE,#1234", "PUSH DE", "PUSH DE",
            "PUSH HL", "LD HL,VALUE", "PUSH HL", "PUSH HL",
            "PUSH IX", "LD IX,VALUE", "PUSH IX", "PUSH IX",
            "PUSH IY", "LD IY,VALUE", "PUSH IY", "PUSH IY"));
  }

  @Test
  public void removesAllRepeatedLoadsFromAChain() {
    assertOptimized(List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "LD BC,VALUE", "PUSH BC",
            "LD BC,VALUE", "PUSH BC", "LD BC,VALUE", "PUSH BC"),
        List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "PUSH BC", "PUSH BC", "PUSH BC"));
  }

  @Test
  public void retainsLoadsWhenValueChangesOrLabelsCreateEntryPoints() {
    assertOptimized(List.of("PUSH BC", "LD BC,FIRST", "PUSH BC", "LD BC,SECOND", "PUSH BC",
            "PUSH DE", "LD DE,VALUE", "PUSH DE", "entry: PUSH DE", "LD DE,VALUE", "PUSH DE",
            "PUSH DE", "LD DE,VALUE", "PUSH DE",
            "PUSH HL", "LD HL,VALUE", "PUSH HL", "again: LD HL,VALUE", "PUSH HL"),
        List.of("PUSH BC", "LD BC,FIRST", "PUSH BC", "LD BC,SECOND", "PUSH BC",
            "PUSH DE", "LD DE,VALUE", "PUSH DE", "entry: PUSH DE", "LD DE,VALUE", "PUSH DE",
            "PUSH DE", "LD DE,VALUE", "PUSH DE",
            "PUSH HL", "LD HL,VALUE", "PUSH HL", "again: LD HL,VALUE", "PUSH HL"));
  }

  @Test
  public void compactProfileUsesTheRepeatedLoadOptimizer() {
    final List<ParsedAsmLine> input =
        List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "LD BC,VALUE", "PUSH BC")
            .stream().map(ParsedAsmLine::new).toList();
    assertEquals(List.of("PUSH BC", "LD BC,VALUE", "PUSH BC", "PUSH BC").stream()
            .map(ParsedAsmLine::new).toList(),
        OptimizationChainFactory.getOptimizations(null, OptimizationLevel.COMPACT)
            .processSources(input));
  }

  private void assertOptimized(final List<String> input, final List<String> expected) {
    assertEquals(expected.stream().map(ParsedAsmLine::new).toList(),
        this.optimizer.optimizeAsmText(null, input.stream().map(ParsedAsmLine::new).toList()));
  }
}
