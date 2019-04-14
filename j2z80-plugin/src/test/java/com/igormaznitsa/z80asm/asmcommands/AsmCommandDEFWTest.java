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
package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsmCommandDEFWTest {

  final AbstractAsmCommand defwCommand = AbstractAsmCommand.findCommandForName("DEFW");

  @Test
  public void testMachineCodeGeneration() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("defw #0102,#0304,#0506,-34");
    final byte[] generated = defwCommand.makeMachineCode(mockContext, parsedLine);
    assertTrue(Arrays.equals(new byte[] {2, 1, 4, 3, 6, 5, -34, -1}, generated));
  }

  @Test
  public void testLabelUsageInDefinition() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    when(mockContext.findLabelAddress("SOME_LABEL")).thenReturn(Integer.valueOf(0x1234));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("defw SOME_LABEL");
    final byte[] generated = defwCommand.makeMachineCode(mockContext, parsedLine);
    assertTrue(Arrays.equals(new byte[] {0x34, 0x12}, generated));
  }
}
