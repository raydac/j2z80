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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AsmCommandENTTest {
  static final AbstractAsmCommand entCommand = AbstractAsmCommand.findCommandForName("ENT");

  @Test
  public void testMachineCode() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("label : ent $+1000");
    final byte[] generated = entCommand.makeMachineCode(mockContext, parsedLine);
    assertEquals(0, generated.length);
    verify(mockContext).setEntryPoint(2100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMachineCode_negativeAddress() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    final ParsedAsmLine parsedLine = new ParsedAsmLine("ent -1");
    entCommand.makeMachineCode(mockContext, parsedLine);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMachineCode_tooBigAddress() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    final ParsedAsmLine parsedLine = new ParsedAsmLine("ent #FFFF + 1");
    entCommand.makeMachineCode(mockContext, parsedLine);
  }
}
