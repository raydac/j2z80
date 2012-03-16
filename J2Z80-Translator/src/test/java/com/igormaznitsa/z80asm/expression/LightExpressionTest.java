package com.igormaznitsa.z80asm.expression;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.asmcommands.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class LightExpressionTest {
    final AsmTranslator MOCK_TRANSLATOR = mock(AsmTranslator.class);
    final AbstractAsmCommand MOCK_COMMAND = mock(AbstractAsmCommand.class);
    final ParsedAsmLine MOCK_PARSED_LINE = mock(ParsedAsmLine.class);

    @Test
    public void testLinkToNestedClassMethod() {
        final String label  = "com.igormaznitsa.j2z80test.Main.<init>#_ILcom/igormaznitsa/j2z80test/Main@AbstractTemplateGenII_V";
        final int address = 0xCAFE;
        when(MOCK_TRANSLATOR.findLabelAddress(eq(label))).thenReturn(Integer.valueOf(address));
        assertEquals("Must return the label address", address, new LightExpression(MOCK_TRANSLATOR, MOCK_COMMAND, MOCK_PARSED_LINE, label).calculate());
    }
}
