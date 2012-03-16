package com.igormaznitsa.impl;

import com.igormaznitsa.j2z80test.Main.AbstractTemplateGen;

public interface MemoryFiller {
    public void fillArea(AbstractTemplateGen generator, int startAddress, int length);
}
