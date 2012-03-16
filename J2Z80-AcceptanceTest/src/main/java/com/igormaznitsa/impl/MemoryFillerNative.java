package com.igormaznitsa.impl;

import com.igormaznitsa.j2z80test.Main.AbstractTemplateGen;

public class MemoryFillerNative extends MemoryFillerJava {

    @Override
    public native void fillArea(final AbstractTemplateGen generator, final int startAddress, final int endAddress);

}
