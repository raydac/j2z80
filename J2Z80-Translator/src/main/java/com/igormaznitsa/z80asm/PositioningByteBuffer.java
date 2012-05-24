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
package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.aux.Assert;
import java.util.Arrays;

/**
 * The class implements a byte buffer which can be extended automatically to bounds of written data
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@SuppressWarnings("serial")
public class PositioningByteBuffer {

    private byte[] insideArray;
    private int maxAddressWritten = -1;
    private int offset = -1;

    public PositioningByteBuffer(final int capacity) {
        insideArray = new byte[capacity];
    }

    private void increaseCapacity() {
        final byte[] newArray = new byte[insideArray.length << 1];
        System.arraycopy(insideArray, 0, newArray, 0, insideArray.length);
        insideArray = newArray;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(insideArray, size());
    }

    public int size() {
        if (maxAddressWritten < 0) {
            return 0;
        }
        return (maxAddressWritten + 1) - offset;
    }

    private void setAddress(final int address) {
        if (offset < 0) {
            offset = address;
        } else {
            if (address < offset) {
                final int delta = offset - address;
                final byte[] newArray = new byte[insideArray.length + delta];
                System.arraycopy(insideArray, 0, newArray, delta, insideArray.length);
                offset = address;
                insideArray = newArray;
            } else {
                if ((address - offset) >= insideArray.length) {
                    increaseCapacity();
                }
            }
        }
    }

    private void writeByteAtPos(final int address, final byte data) {
        setAddress(address);
        if (maxAddressWritten < address) {
            maxAddressWritten = address;
        }
        insideArray[address - offset] = data;
    }

    public int getDataStartOffset() {
        return offset;
    }
    
    public void write(final int address, final byte[] data) {
        Assert.assertAddress(address);

        setAddress(address);

        int addr = address;
        for (int i = 0; i < data.length; i++) {
            writeByteAtPos(addr, data[i]);
            addr++;
        }
    }
}
