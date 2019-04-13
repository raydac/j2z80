/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
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
