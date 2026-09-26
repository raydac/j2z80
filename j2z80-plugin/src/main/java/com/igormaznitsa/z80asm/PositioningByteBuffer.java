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

import com.igormaznitsa.j2z80.translator.utils.AsmAssertions;

import java.util.Arrays;

/**
 * The class implements a byte buffer which can be extended automatically to bounds of written data
 */
public class PositioningByteBuffer {

  private byte[] insideArray;
  private int maxAddressWritten = -1;
  private int offset = -1;

  public PositioningByteBuffer(final int capacity) {
    insideArray = new byte[capacity];
  }

  private void ensureIndexFits(final int index) {
    if (index < this.insideArray.length) {
      return;
    }

    int newLength = Math.max(this.insideArray.length, 1);
    while (newLength <= index) {
      if (newLength > (Integer.MAX_VALUE >> 1)) {
        newLength = index + 1;
        break;
      }
      newLength <<= 1;
    }
    this.insideArray = Arrays.copyOf(this.insideArray, newLength);
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
    if (this.offset < 0) {
      this.offset = address;
    } else if (address < this.offset) {
      final int delta = this.offset - address;
      final byte[] newArray = new byte[this.insideArray.length + delta];
      System.arraycopy(this.insideArray, 0, newArray, delta, this.insideArray.length);
      this.offset = address;
      this.insideArray = newArray;
    }
    this.ensureIndexFits(address - this.offset);
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
    AsmAssertions.assertAddress(address);

    setAddress(address);

    int addr = address;
    for (byte datum : data) {
      writeByteAtPos(addr, datum);
      addr++;
    }
  }
}
