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
package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.utils.Assert;

import java.util.Arrays;

/**
 * The class implements a byte buffer which can be extended automatically to bounds of written data
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
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
    for (byte datum : data) {
      writeByteAtPos(addr, datum);
      addr++;
    }
  }
}
