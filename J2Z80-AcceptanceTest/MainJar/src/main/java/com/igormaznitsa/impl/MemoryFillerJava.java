package com.igormaznitsa.impl;

import com.igormaznitsa.j2z80test.Main.AbstractTemplateGen;
import com.igormaznitsa.memory.MemoryAccessor;

public class MemoryFillerJava implements MemoryFiller {

    MemoryFillerJava() {
    }

    private boolean checkThatNotNull(final Object obj) {
        return obj!=null;
    }

    @Override
    public void fillArea(final AbstractTemplateGen generator, final int startAddress, final int length) {
        if (checkThatNotNull(generator)) {

            // we cant use FOR because it works with signed values and in our case int is a signed 16 bit one
            int address = startAddress;
            int len = length;
            while (len > 0) {
                MemoryAccessor.writeWordToMemory(address, generator.getValueForAddress(address));
                address += 2;
                len -= 2;
            }
        }
    }
}
