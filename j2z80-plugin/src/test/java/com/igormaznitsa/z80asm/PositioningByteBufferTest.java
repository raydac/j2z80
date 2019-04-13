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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PositioningByteBufferTest {

  @Test
  public void testWriteAtPosition() {
    final PositioningByteBuffer buffer = new PositioningByteBuffer(128);
    final byte[] array = new byte[] {0x1, 0x2, 0x3, 0x4};
    buffer.write(100, array);
    assertTrue(Arrays.equals(array, buffer.toByteArray()));
    assertEquals(100, buffer.getDataStartOffset());
  }

  @Test
  public void testWrite() {
    final PositioningByteBuffer buffer = new PositioningByteBuffer(1);
    final byte[] array = new byte[] {0x1, 0x2};
    buffer.write(0, new byte[] {(byte) 0xFF});
    buffer.write(3, array);
    assertTrue(Arrays.equals(new byte[] {(byte) 0xFF, 0, 0, 1, 2}, buffer.toByteArray()));
    assertEquals(0, buffer.getDataStartOffset());
  }

  @Test
  public void testWriteAtPoitionBefore() {
    final PositioningByteBuffer buffer = new PositioningByteBuffer(10);
    final byte[] array = new byte[] {0x1, 0x2};
    buffer.write(10, array);
    buffer.write(5, new byte[] {0x3});
    assertTrue(Arrays.equals(new byte[] {(byte) 0x03, 0, 0, 0, 0, 1, 2}, buffer.toByteArray()));
    assertEquals(5, buffer.getDataStartOffset());
  }
}
