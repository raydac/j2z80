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

package com.igormaznitsa.z80asm.asmcommands;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.igormaznitsa.z80asm.AsmTranslator;
import org.junit.Test;
import org.mockito.Mockito;

public class AsmCommandOperandBoundsTest {

  private final AsmTranslator context = Mockito.mock(AsmTranslator.class);

  @Test
  public void testNegativeIndexDisplacement() {
    assertArrayEquals(
        new byte[] {(byte) 0xDD, (byte) 0x86, (byte) 0xFF},
        encode("ADD", "add a,(ix-1)"));
    assertArrayEquals(
        new byte[] {(byte) 0xFD, (byte) 0xA6, (byte) 0xFE},
        encode("AND", "and (iy-2)"));
  }

  @Test
  public void testUnsignedImmediate() {
    assertArrayEquals(new byte[] {(byte) 0xD6, (byte) 0xFF}, encode("SUB", "sub #FF"));
    assertArrayEquals(new byte[] {(byte) 0xFE, (byte) 0xC8}, encode("CP", "cp 200"));
    assertArrayEquals(new byte[] {(byte) 0xFE, (byte) 0x80}, encode("CP", "cp -128"));
    assertArrayEquals(new byte[] {(byte) 0xFE, (byte) 0xFF}, encode("CP", "cp -1"));
  }

  @Test
  public void testLowerCaseIndirectRegisters() {
    assertArrayEquals(new byte[] {(byte) 0x0A}, encode("LD", "ld a,(bc)"));
    assertArrayEquals(new byte[] {(byte) 0xFD, (byte) 0xE9}, encode("JP", "jp (iy)"));
  }

  @Test(expected = AssertionError.class)
  public void testLdImmediateToStackPointerIsRejected() {
    encode("LD", "ld (sp),1");
  }

  private byte[] encode(final String commandName, final String line) {
    final ParsedAsmLine parsed = new ParsedAsmLine(line);
    assertEquals(commandName, parsed.getCommand());
    return AbstractAsmCommand.findCommandForName(commandName).makeMachineCode(this.context, parsed);
  }
}
