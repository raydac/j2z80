package com.igormaznitsa.templategen;

import com.igormaznitsa.j2z80test.Main;

public class EvenPatternGenerator extends Main.AbstractTemplateGen {

    @Override
    public int getValueForAddress(final int address) {
        return address ^ 0x5555;
    }
    
}
