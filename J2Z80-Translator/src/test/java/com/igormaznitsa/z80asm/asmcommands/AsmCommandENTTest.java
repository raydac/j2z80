package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AsmCommandENTTest {
    static final AbstractAsmCommand entCommand = AbstractAsmCommand.findCommandForName("ENT");
    
    @Test
    public void testMachineCode() {
        final AsmTranslator mockContext = mock(AsmTranslator.class);
        when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
        final ParsedAsmLine parsedLine = new ParsedAsmLine("label : ent $+1000");
        final byte [] generated = entCommand.makeMachineCode(mockContext, parsedLine);
        assertEquals(0, generated.length);
        verify(mockContext).setEntryPoint(2100);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMachineCode_negativeAddress() {
        final AsmTranslator mockContext = mock(AsmTranslator.class);
        final ParsedAsmLine parsedLine = new ParsedAsmLine("ent -1");
        entCommand.makeMachineCode(mockContext, parsedLine);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMachineCode_tooBigAddress() {
        final AsmTranslator mockContext = mock(AsmTranslator.class);
        final ParsedAsmLine parsedLine = new ParsedAsmLine("ent #FFFF + 1");
        entCommand.makeMachineCode(mockContext, parsedLine);
    }
}
