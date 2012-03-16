package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AbstractAsmCommandTest {
    
    @Test
    public void testExtractCalculatedPart() {
        assertEquals("java.lang.System.out#Ljava/io/PrintStream;",AbstractAsmCommand.extractCalculatedPart("(java.lang.System.out#Ljava/io/PrintStream;)"));
    }
}
