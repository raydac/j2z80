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
}
