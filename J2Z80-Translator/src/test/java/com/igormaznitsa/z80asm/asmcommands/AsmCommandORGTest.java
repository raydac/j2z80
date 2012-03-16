package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AsmCommandORGTest {
    
    final AbstractAsmCommand orgCommand = AbstractAsmCommand.findCommandForName("ORG");
    
    @Test
    public void testSomeMethod() {
        final AsmTranslator mockContext = mock(AsmTranslator.class);
        final ParsedAsmLine parsedLine = new ParsedAsmLine("org #FF00");
        final byte [] generated = orgCommand.makeMachineCode(mockContext, parsedLine);
        assertEquals(0, generated.length);
        verify(mockContext).setPC(0xFF00);
    }
}
