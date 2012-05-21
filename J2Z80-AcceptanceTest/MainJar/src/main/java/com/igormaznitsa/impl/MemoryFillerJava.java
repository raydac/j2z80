/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 * 
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
