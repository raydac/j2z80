package com.igormaznitsa.z80asm.asmcommands.expression;

import com.igormaznitsa.z80asm.expression.LightExpression;
import com.igormaznitsa.z80asm.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class LightExpressionTest {
    @Test
    public void testCalculation() {
        final AsmTranslator context = mock(AsmTranslator.class);
        when(context.findLabelAddress("LABEL")).thenReturn(Integer.valueOf(0xFFED));
        when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
        final LightExpression expression = new LightExpression(context,null,null, "-#FF+\"BA\"-%101001001+LABEL-$");
        assertEquals(((-255)+((66<<8)|65)-329+65517-4660), expression.calculate());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUnknownLabel() {
        final AsmTranslator context = mock(AsmTranslator.class);
        when(context.findLabelAddress("LABEL")).thenReturn(null);
        when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
        new LightExpression(context,null,null, "-#FF+\"BA\"-%101001001+LABEL-$").calculate();
    }

    @Test
    public void testUnknownLocalLabel() {
        final AsmTranslator context = mock(AsmTranslator.class);
        when(context.findLabelAddress("@LABEL")).thenReturn(null);
        when(context.getPC()).thenReturn(Integer.valueOf(0x1234));
        new LightExpression(context,null,null, "$-@LABEL").calculate();
        verify(context).registerLocalLabelExpectant(eq("@LABEL"), any(LocalLabelExpectant.class));
    }
    
    @Test
    public void testBigLabel(){
        final AsmTranslator context = mock(AsmTranslator.class);
        when(context.findLabelAddress("java.lang.System.out#Ljava/io/PrintStream;")).thenReturn(Integer.valueOf(0x1000));
        assertEquals(0x1000L,new LightExpression(context,null,null, "java.lang.System.out#Ljava/io/PrintStream;").calculate());
    }
}
