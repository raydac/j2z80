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

import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Test;

public class ParsedAsmLineTest {
    
    @Test
    public void testEmptyString() {
        final ParsedAsmLine parsed = new ParsedAsmLine("");
        assertNull(parsed.getLabel());
        assertEquals("",parsed.getCommand());
        assertEquals(0,parsed.getArgs().length);
    }

    @Test
    public void testOnlyComment() {
        final ParsedAsmLine parsed = new ParsedAsmLine("     ; some comment");
        assertNull(parsed.getLabel());
        assertEquals("",parsed.getCommand());
        assertEquals(0,parsed.getArgs().length);
    }

    @Test
    public void testOnlyCommentSinceTheStringStart() {
        final ParsedAsmLine parsed = new ParsedAsmLine("; triplet : (word) class_id (word) method_adress (word) max_locals_for_method");
        assertNull(parsed.getLabel());
        assertEquals("",parsed.getCommand());
        assertEquals(0,parsed.getArgs().length);
    }

    @Test
    public void testLabel() {
        final ParsedAsmLine parsed = new ParsedAsmLine("label:");
        assertEquals("label",parsed.getLabel());
        assertEquals("",parsed.getCommand());
        assertEquals(0,parsed.getArgs().length);
    }

    @Test
    public void testCommand() {
        final ParsedAsmLine parsed = new ParsedAsmLine("lr");
        assertNull(parsed.getLabel());
        assertEquals("LR",parsed.getCommand());
        assertEquals(0,parsed.getArgs().length);
    }

    @Test
    public void testCommandWithArgs() {
        final ParsedAsmLine parsed = new ParsedAsmLine("   ld a , b");
        assertNull(parsed.getLabel());
        assertEquals("LD",parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"A","B"}, parsed.getArgs()));
    }

    @Test
    public void testLabelAndCommandWithArgsAndComment() {
        final ParsedAsmLine parsed = new ParsedAsmLine(" some  :  ld a , b ; ha ha ha");
        assertEquals("some",parsed.getLabel());
        assertEquals("LD",parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"A","B"}, parsed.getArgs()));
    }

    @Test
    public void testLabelAndCommandWithArgsWithCommentCharAndComment() {
        final ParsedAsmLine parsed = new ParsedAsmLine(" some  :  defm \"hello ;world\" ; ha ha ha");
        assertEquals("some",parsed.getLabel());
        assertEquals("DEFM",parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"\"hello ;world\""}, parsed.getArgs()));
    }

    @Test
    public void testCommandWithPairAndLabel() {
        final ParsedAsmLine parsed = new ParsedAsmLine("ld hl,ixhell");
        assertNull(parsed.getLabel());
        assertEquals("LD", parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"HL","ixhell"}, parsed.getArgs()));
    }

    @Test
    public void testLabelAndCommandWithArgsWithCommentCharAndNonClosedString() {
        final ParsedAsmLine parsed = new ParsedAsmLine(" soME  :  defm \"hello;world");
        assertEquals("soME",parsed.getLabel());
        assertEquals("DEFM",parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"\"hello;world"}, parsed.getArgs()));
    }

    @Test
    public void testLongLabelInCommand() {
        final ParsedAsmLine parsed = new ParsedAsmLine("    JP NC,com.igormaznitsa.j2z80test.App.mainz#__V->15");
        assertNull(parsed.getLabel());
        assertEquals("JP",parsed.getCommand());
        assertTrue(Arrays.equals(new String[]{"NC","com.igormaznitsa.j2z80test.App.mainz#__V->15"}, parsed.getArgs()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLabelLikeCommand() {
        final ParsedAsmLine parsed = new ParsedAsmLine("    JP NC,com.igormaznitsa.j2z80test.App.mainz#__V->15 :");
    }

    @Test
    public void testToString() {
        assertEquals("",new ParsedAsmLine("").toString());
        assertEquals("lAbEl: ",new ParsedAsmLine("   lAbEl:   ").toString());
        assertEquals("lAbEl: LD A,(HL)",new ParsedAsmLine("   lAbEl:   lD a        ,    (  Hl) ; comment ").toString());
    }
    
    @Test
    public void testNonParsingConstructor(){
        assertEquals("", new ParsedAsmLine(null, null).toString());
        assertEquals("lAbEl: ", new ParsedAsmLine(" lAbEl ", null).toString());
        assertEquals("lAbEl: HALT ", new ParsedAsmLine(" lAbEl ", " hAlT    ").toString());
        assertEquals("lAbEl: LD (HL),A", new ParsedAsmLine(" lAbEl ", " ld    ","   (hl)","   a   ").toString());
    }

    @Test
    public void testHash(){
        assertEquals(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").hashCode(), new ParsedAsmLine("  lAbEl  ", "   ld   ","  (hL)","  a ").hashCode());
    }

    @Test
    public void testEquals(){
        assertEquals(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   "), new ParsedAsmLine("  lAbEl  ", "   ld   ","  (hL)","  a "));
        assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl2  ", "   ld   ","  (hL)","  a ")));
        assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   lda   ","  (hL)","  a ")));
        assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   ld   ","  (hL )","  a ")));
        assertFalse(new ParsedAsmLine("lAbEl: lD    (  hl  ),   a   ").equals(new ParsedAsmLine("  lAbEl  ", "   ld   ","  (hL)","  b ")));
    }
}
