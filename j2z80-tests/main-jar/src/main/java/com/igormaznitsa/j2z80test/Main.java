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
package com.igormaznitsa.j2z80test;

import com.igormaznitsa.impl.MemoryFiller;
import com.igormaznitsa.impl.MemoryFillerFactory;
import com.igormaznitsa.memory.MemoryAccessor;
import com.igormaznitsa.templategen.EvenPatternGenerator;
import com.igormaznitsa.templategen.OddPatternGenerator;

public class Main {
    public static abstract class AbstractTemplateGen {
        public abstract int getValueForAddress(int address);
    }

    public static final short BLOCK_LENGTH = 0x800;
    public static final short START_ADDRESS_1 = 0x4000;
    public static final short START_ADDRESS_2 = 0x4000 + BLOCK_LENGTH;
    public static final short START_ADDRESS_3 = 0x4000 + (BLOCK_LENGTH << 1);
    public static final short FLAG_ADDRESS = 0x4000;
    public static final short FLAG_DATA = (short) 0xCAFE;
    private final MemoryFiller filler;
    private final AbstractTemplateGen generator;
    private final int startAddress;
    private final int length;

    public Main(final int fillerType, final AbstractTemplateGen gen, final int startAddress, final int length) {
        this.filler = MemoryFillerFactory.getFiller(fillerType);
        this.startAddress = startAddress;
        this.length = length;
        this.generator = gen;
    }

    public synchronized boolean process() {
        if (filler != null) {
            filler.fillArea(generator, startAddress, length);
            return true;
        }
        return false;
    }

    public static void mainz() {
        MemoryAccessor.writeWordToMemory(FLAG_ADDRESS, 0);

        // first
        final Main first = new Main(MemoryFillerFactory.FILLER_JAVA, new OddPatternGenerator(), START_ADDRESS_1, BLOCK_LENGTH);
        // second
        final Main second = new Main(MemoryFillerFactory.FILLER_NATIVE, new EvenPatternGenerator(), START_ADDRESS_2, BLOCK_LENGTH);
        // third
        final Main third = new Main(-112, new EvenPatternGenerator(), START_ADDRESS_3, BLOCK_LENGTH);

        synchronized (first) {
            if (first.hashCode() != second.hashCode()) {
                if (first.process()) {
                    if (second.process()) {
                        if (!third.process()) {
                            MemoryAccessor.writeWordToMemory(FLAG_ADDRESS, FLAG_DATA);
                        }
                    }
                }
            }
        }

    }
}
