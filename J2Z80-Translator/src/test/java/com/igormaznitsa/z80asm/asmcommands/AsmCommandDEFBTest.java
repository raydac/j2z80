package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import java.util.Arrays;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AsmCommandDEFBTest {

    final AbstractAsmCommand defbCommand = AbstractAsmCommand.findCommandForName("DEFB");
    
    @Test
    public void testMachineCodeGeneration() {
       final AsmTranslator mockContext = mock(AsmTranslator.class);
        when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
        final ParsedAsmLine parsedLine = new ParsedAsmLine("defb 1,2,3,4,5,6,7,8,9,10,11,12");
        final byte [] generated = defbCommand.makeMachineCode(mockContext, parsedLine);
        assertTrue(Arrays.equals(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12}, generated));
    }
}
