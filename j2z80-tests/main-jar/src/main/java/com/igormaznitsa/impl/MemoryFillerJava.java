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
