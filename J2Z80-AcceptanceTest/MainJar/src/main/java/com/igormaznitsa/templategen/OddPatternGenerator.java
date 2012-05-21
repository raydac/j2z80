package com.igormaznitsa.templategen;

import com.igormaznitsa.j2z80test.Main;

public class OddPatternGenerator extends Main.AbstractTemplateGen {

    @Override
    public int getValueForAddress(final int address) {
        return address ^ (short)0xAAAA;
    }
    
}
