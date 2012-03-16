package com.igormaznitsa.z80asm.asmcommands;

import static org.junit.Assert.*;
import org.junit.Test;

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
