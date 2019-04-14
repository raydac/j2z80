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

import java.util.Arrays;

import static org.junit.Assert.*;

public class ParsedAsmLineTest {

  @Test
  public void testEmptyString() {
    final ParsedAsmLine parsed = new ParsedAsmLine("");
    assertNull(parsed.getLabel());
    assertEquals("", parsed.getCommand());
    assertEquals(0, parsed.getArgs().length);
  }

  @Test
  public void testOnlyComment() {
    final ParsedAsmLine parsed = new ParsedAsmLine("     ; some comment");
    assertNull(parsed.getLabel());
    assertEquals("", parsed.getCommand());
    assertEquals(0, parsed.getArgs().length);
  }

  @Test
  public void testOnlyCommentSinceTheStringStart() {
    final ParsedAsmLine parsed = new ParsedAsmLine("; triplet : (word) class_id (word) method_adress (word) max_locals_for_method");
    assertNull(parsed.getLabel());
    assertEquals("", parsed.getCommand());
    assertEquals(0, parsed.getArgs().length);
  }

  @Test
  public void testLabel() {
    final ParsedAsmLine parsed = new ParsedAsmLine("label:");
    assertEquals("label", parsed.getLabel());
    assertEquals("", parsed.getCommand());
    assertEquals(0, parsed.getArgs().length);
  }

  @Test
  public void testCommand() {
    final ParsedAsmLine parsed = new ParsedAsmLine("lr");
    assertNull(parsed.getLabel());
    assertEquals("LR", parsed.getCommand());
    assertEquals(0, parsed.getArgs().length);
  }

  @Test
  public void testCommandWithArgs() {
    final ParsedAsmLine parsed = new ParsedAsmLine("   ld a , b");
    assertNull(parsed.getLabel());
    assertEquals("LD", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"A", "B"}, parsed.getArgs()));
  }

  @Test
  public void testLabelAndCommandWithArgsAndComment() {
    final ParsedAsmLine parsed = new ParsedAsmLine(" some  :  ld a , b ; ha ha ha");
    assertEquals("some", parsed.getLabel());
    assertEquals("LD", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"A", "B"}, parsed.getArgs()));
  }

  @Test
  public void testLabelAndCommandWithArgsWithCommentCharAndComment() {
    final ParsedAsmLine parsed = new ParsedAsmLine(" some  :  defm \"hello ;world\" ; ha ha ha");
    assertEquals("some", parsed.getLabel());
    assertEquals("DEFM", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"\"hello ;world\""}, parsed.getArgs()));
  }

  @Test
  public void testCommandWithPairAndLabel() {
    final ParsedAsmLine parsed = new ParsedAsmLine("ld hl,ixhell");
    assertNull(parsed.getLabel());
    assertEquals("LD", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"HL", "ixhell"}, parsed.getArgs()));
  }

  @Test
  public void testLabelAndCommandWithArgsWithCommentCharAndNonClosedString() {
    final ParsedAsmLine parsed = new ParsedAsmLine(" soME  :  defm \"hello;world");
    assertEquals("soME", parsed.getLabel());
    assertEquals("DEFM", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"\"hello;world"}, parsed.getArgs()));
  }

  @Test
  public void testLongLabelInCommand() {
    final ParsedAsmLine parsed = new ParsedAsmLine("    JP NC,com.igormaznitsa.j2z80test.App.mainz#__V->15");
    assertNull(parsed.getLabel());
    assertEquals("JP", parsed.getCommand());
    assertTrue(Arrays.equals(new String[] {"NC", "com.igormaznitsa.j2z80test.App.mainz#__V->15"}, parsed.getArgs()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLabelLikeCommand() {
    final ParsedAsmLine parsed = new ParsedAsmLine("    JP NC,com.igormaznitsa.j2z80test.App.mainz#__V->15 :");
  }

  @Test
  public void testToString() {
    assertEquals("", new ParsedAsmLine("").toString());
    assertEquals("lAbEl: ", new ParsedAsmLine("   lAbEl:   ").toString());
    assertEquals("lAbEl: LD A,(HL)", new ParsedAsmLine("   lAbEl:   lD a        ,    (  Hl) ; comment ").toString());
  }

  @Test
  public void testNonParsingConstructor() {
    assertEquals("", new ParsedAsmLine(null, null).toString());
    assertEquals("lAbEl: ", new ParsedAsmLine(" lAbEl ", null).toString());
    assertEquals("lAbEl: HALT ", new ParsedAsmLine(" lAbEl ", " hAlT    ").toString());
    assertEquals("lAbEl: LD (HL),A", new ParsedAsmLine(" lAbEl ", " ld    ", "   (hl)", "   a   ").toString());
  }

  @Test
  public void testHash() {
    assertEquals(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").hashCode(), new ParsedAsmLine("  lAbEl  ", "   ld   ", "  (hL)", "  a ").hashCode());
  }

  @Test
  public void testEquals() {
    assertEquals(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   "), new ParsedAsmLine("  lAbEl  ", "   ld   ", "  (hL)", "  a "));
    assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl2  ", "   ld   ", "  (hL)", "  a ")));
    assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   lda   ", "  (hL)", "  a ")));
    assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   ld   ", "  (hL )", "  a ")));
    assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   ld   ", "  (hL)", "  b ")));
  }
}
