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
package com.igormaznitsa.z80asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

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
  public void testWriteFarBeyondCapacity() {
    final PositioningByteBuffer buffer = new PositioningByteBuffer(8);
    buffer.write(0, new byte[] {1});
    buffer.write(100, new byte[] {2, 3});

    final byte[] data = buffer.toByteArray();
    assertEquals(102, data.length);
    assertEquals(1, data[0]);
    assertEquals(2, data[100]);
    assertEquals(3, data[101]);
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
