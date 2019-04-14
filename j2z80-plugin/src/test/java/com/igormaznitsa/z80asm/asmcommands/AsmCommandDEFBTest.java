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

public class AsmCommandDEFBTest {

  final AbstractAsmCommand defbCommand = AbstractAsmCommand.findCommandForName("DEFB");

  @Test
  public void testMachineCodeGeneration() {
    final AsmTranslator mockContext = mock(AsmTranslator.class);
    when(mockContext.getPC()).thenReturn(Integer.valueOf(1100));
    final ParsedAsmLine parsedLine = new ParsedAsmLine("defb 1,2,3,4,5,6,7,8,9,10,11,12");
    final byte[] generated = defbCommand.makeMachineCode(mockContext, parsedLine);
    assertTrue(Arrays.equals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, generated));
  }
}
