/*
 * Copyright 2012-2026 Igor Maznitsa.
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

package com.igormaznitsa.j2z80.translator.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Sna48WriterTest {

  private static final int SNA_HEADER_SIZE = 0x1B;
  private static final int RAM_START_ADDRESS = 0x4000;
  private static final int RAM_END_ADDRESS = 0xFFFF;
  private static final int RAM_FILE_OFFSET = SNA_HEADER_SIZE;
  private static final int RAM_FILE_SIZE = 0xC000;

  @Test
  public void writesSnaHeaderAndProgramAtConfiguredAddresses() {
    final int startAddress = 0x8123;
    final int stackTopAddress = 0xF123;
    final byte[] program = {(byte) 0xA5, (byte) 0x5A};

    final byte[] sna = new Sna48Writer(startAddress, stackTopAddress, program).writeSna();

    assertEquals(SNA_HEADER_SIZE + RAM_FILE_SIZE, sna.length);
    assertEquals((stackTopAddress - 2) & 0xFF, sna[0x17] & 0xFF);
    assertEquals(((stackTopAddress - 2) >> 8) & 0xFF, sna[0x18] & 0xFF);

    final int stackPointerOffset = SNA_HEADER_SIZE + stackTopAddress - 2 - RAM_START_ADDRESS;
    assertEquals(startAddress & 0xFF, sna[stackPointerOffset] & 0xFF);
    assertEquals((startAddress >> 8) & 0xFF, sna[stackPointerOffset + 1] & 0xFF);

    final int programOffset = SNA_HEADER_SIZE + startAddress - RAM_START_ADDRESS;
    assertEquals(program[0] & 0xFF, sna[programOffset] & 0xFF);
    assertEquals(program[1] & 0xFF, sna[programOffset + 1] & 0xFF);
    assertArrayEquals(new byte[] {(byte) 0xA5, (byte) 0x5A}, program);
  }

  @Test
  public void mapsFirstAndLastRamAddressesIntoSnapshot() {
    final byte[] firstAddressSna =
        new Sna48Writer(RAM_START_ADDRESS, 0xFFFD, new byte[] {1}).writeSna();
    final byte[] lastAddressSna =
        new Sna48Writer(RAM_END_ADDRESS, 0xFFFD, new byte[] {2}).writeSna();

    assertEquals(1, firstAddressSna[RAM_FILE_OFFSET] & 0xFF);
    assertEquals(2, lastAddressSna[RAM_FILE_OFFSET + RAM_FILE_SIZE - 1] & 0xFF);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsStartAddressBelowSpectrumRam() {
    new Sna48Writer(RAM_START_ADDRESS - 1, 0xFFFD, new byte[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsStartAddressAboveThe16BitAddressSpace() {
    new Sna48Writer(RAM_END_ADDRESS + 1, 0xFFFD, new byte[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsStackTopBelowZero() {
    new Sna48Writer(RAM_START_ADDRESS, -1, new byte[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsStackTopAboveTheSupportedRange() {
    new Sna48Writer(RAM_START_ADDRESS, 0xFFFE, new byte[0]);
  }
}
