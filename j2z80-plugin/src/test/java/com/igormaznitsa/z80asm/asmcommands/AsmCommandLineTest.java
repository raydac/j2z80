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
package com.igormaznitsa.z80asm.asmcommands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AsmCommandLineTest {

  @Test
  public void parsingTest_maximumCase() {
    final ParsedAsmLine line = new ParsedAsmLine("  some_label   : ld  (  ix - # 4  )  , \"hello;world\"    ; comment");
    assertEquals("some_label", line.getLabel());
    assertEquals("LD", line.getCommand());
    assertEquals(2, line.getArgs().length);
    assertEquals("(IX-#4),\"hello;world\"", line.getSignature());
    assertEquals("(IX-#4)", line.getArgs()[0]);
    assertEquals("\"hello;world\"", line.getArgs()[1]);
  }

  @Test
  public void parsingTest_onlyCommand() {
    final ParsedAsmLine line = new ParsedAsmLine(" rl ");
    assertNull(line.getLabel());
    assertEquals("RL", line.getCommand());
    assertEquals(0, line.getArgs().length);
  }

  @Test
  public void parsingTest_onlyCommandAndComment() {
    final ParsedAsmLine line = new ParsedAsmLine(" rl;hello ");
    assertNull(line.getLabel());
    assertEquals("RL", line.getCommand());
    assertEquals(0, line.getArgs().length);
  }

  @Test
  public void parsingTest_onlyCommandAndArgs() {
    final ParsedAsmLine line = new ParsedAsmLine(" ld   a , b   , c;hohohoh");
    assertNull(line.getLabel());
    assertEquals("LD", line.getCommand());
    assertEquals(3, line.getArgs().length);
    assertEquals("A", line.getArgs()[0]);
    assertEquals("B", line.getArgs()[1]);
    assertEquals("C", line.getArgs()[2]);
  }

  @Test
  public void parsingTest_onlyLabel() {
    final ParsedAsmLine line = new ParsedAsmLine("label ho:");
    assertEquals("labelho", line.getLabel());
    assertEquals("", line.getCommand());
    assertEquals(0, line.getArgs().length);
  }

  @Test
  public void parsingTest_onlyLabelAndComment() {
    final ParsedAsmLine line = new ParsedAsmLine("label: ;jjj");
    assertEquals("label", line.getLabel());
    assertEquals("", line.getCommand());
    assertEquals(0, line.getArgs().length);
  }
}
