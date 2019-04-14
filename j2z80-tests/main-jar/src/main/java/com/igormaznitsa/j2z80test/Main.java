/* 
 * Copyright 2019 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
