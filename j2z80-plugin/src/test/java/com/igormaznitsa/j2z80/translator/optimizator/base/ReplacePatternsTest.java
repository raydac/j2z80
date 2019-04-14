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

import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ReplacePatternsTest {

  private ReplacePatterns testReplace = new ReplacePatterns();

  private static final String[] str(final String... array) {
    return array;
  }

  @Test
  public void testRemovePUSHPOP_BC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push bc", "push bc", "pop bc", "pop bc", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_BC_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push bc", "push bc", "clrloc", "pop bc", "pop bc", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_DE() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push de", "push de", "pop de", "pop de", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_DE_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push de", "push de", "clrloc", "pop de", "pop de", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_HL() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push hl", "push hl", "pop hl", "pop hl", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_HL_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push hl", "push hl", "clrloc", "pop hl", "pop hl", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_AF() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push af", "push af", "pop af", "pop af", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_AF_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push af", "push af", "clrloc", "pop af", "pop af", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_IX() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push ix", "push ix", "pop ix", "pop ix", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_IX_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push ix", "push ix", "clrloc", "pop ix", "pop ix", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_IY() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push iy", "push iy", "pop iy", "pop iy", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_IY_CLRLOC() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "clrloc", "halt"), str("ld bc,100", "push iy", "push iy", "clrloc", "pop iy", "pop iy", "halt"));
  }

  @Test
  public void testRemovePUSHPOP_Mix() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "halt"), str("ld bc,100", "push af", "push bc", "push de", "pop de", "pop bc", "pop af", "halt"));
  }

  @Test
  public void testNotRemovePUSHPOPCase() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "push hl", "push bc", "pop hl", "pop bc", "halt"), str("ld bc,100", "push hl", "push bc", "pop hl", "pop bc", "halt"));
  }

  @Test
  public void testNotRemovePUSHPOPForLabel() {
    assertOptimize("Must remove all PUSH POP pairs", str("ld bc,100", "push hl", "test: pop hl", "halt"), str("ld bc,100", "push hl", "test: pop hl", "halt"));
  }

  private void assertOptimize(final String message, final String[] etallon, final String[] toOptimize) {
    final List<ParsedAsmLine> etallonList = new ArrayList<ParsedAsmLine>(etallon.length);
    for (final String asm : etallon) {
      etallonList.add(new ParsedAsmLine(asm));
    }

    final List<ParsedAsmLine> toOptimizeList = new ArrayList<ParsedAsmLine>(toOptimize.length);
    for (final String asm : toOptimize) {
      toOptimizeList.add(new ParsedAsmLine(asm));
    }

    final List<ParsedAsmLine> optimized = testReplace.optimizeAsmText(null, toOptimizeList);

    assertTrue(message, etallonList.equals(optimized));
  }
}
