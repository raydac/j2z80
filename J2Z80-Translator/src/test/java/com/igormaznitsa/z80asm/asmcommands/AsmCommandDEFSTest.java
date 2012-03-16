package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AsmCommandDEFSTest {
    final AbstractAsmCommand defsCommand = AbstractAsmCommand.findCommandForName("DEFS");
    
    @Test
    public void testMachineCodeGeneration() {
       final AsmTranslator mockContext = mock(AsmTranslator.class);
        when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
        final ParsedAsmLine parsedLine = new ParsedAsmLine("defs 2000+8000");
        final byte [] generated = defsCommand.makeMachineCode(mockContext, parsedLine);
        assertEquals(10000, generated.length);
    }
}
