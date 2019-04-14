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

import org.junit.Test;

import java.io.File;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class Z80AsmTest {

  @Test
  public void testLabelWithoutCode() throws Exception {
    final File file = getFile("only_label.asm");
    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();
    assertEquals(0, generated.length);
    assertEquals(6000L, asm.findLabelAddress("SOME_LABEL").longValue());
  }

  @Test
  public void testHelloWorld() throws Exception {
    final File file = getFile("hello_world.asm");

    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();

    assertEquals("3E 02 CD 01 16 21 10 10 7E FE 00 C8 D7 23 18 F8 48 65 6C 6C 6F 2C 20 77 6F 72 6C 64 21 0D 00", asText(generated));
  }

  @Test
  public void testDjnzJumpToForwardLocal() throws Exception {
    final File file = getFile("local.asm");

    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();

    assertEquals("10 04 74 65 73 74 00 00 74 65 73 74 10 F9", asText(generated));
  }

  @Test
  public void testMultilabeledAddress() throws Exception {
    final File file = getFile("multilabeled.asm");

    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();

    assertEquals(0x2000, asm.findLabelAddress("label1").intValue());
    assertEquals(0x2000, asm.findLabelAddress("label2").intValue());
    assertEquals(0x2000, asm.findLabelAddress("label3").intValue());
  }

  @Test
  public void testEquAddresses() throws Exception {
    final File file = getFile("equ.asm");

    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();

    assertEquals(0x1234, asm.findLabelAddress("labela").intValue());
    assertEquals(0x1000, asm.findLabelAddress("label1").intValue());
    assertEquals(0x2000, asm.findLabelAddress("label2").intValue());
    assertEquals(0x3000, asm.findLabelAddress("label3").intValue());
    assertEquals(0x4000, asm.findLabelAddress("label4").intValue());
    assertEquals(0x4000, asm.findLabelAddress("label4#").intValue());
    assertEquals(0x1234, asm.findLabelAddress("label2#").intValue());
    assertEquals(0x1235, asm.getPC());
  }

  @Test
  public void testLocalLabels() throws Exception {
    final File file = getFile("localatend.asm");
    final Z80Asm asm = new Z80Asm(file);
    final byte[] generated = asm.process();
  }

  private File getFile(final String name) throws Exception {
    return new File(this.getClass().getResource(name).toURI());
  }

  private final String asText(final byte[] array) {
    final StringBuilder result = new StringBuilder();

    for (final byte b : array) {
      if (result.length() != 0) {
        result.append(' ');
      }

      final String str = Integer.toHexString(b & 0xFF).toUpperCase(Locale.ENGLISH);

      if (str.length() == 1) {
        result.append('0');
      }
      result.append(str);
    }

    return result.toString();
  }
}
