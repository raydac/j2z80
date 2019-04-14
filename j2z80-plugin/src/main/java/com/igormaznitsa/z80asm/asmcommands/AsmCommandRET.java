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

public class AsmCommandRET extends AbstractAsmCommand {

  public AsmCommandRET() {
    super();
    addCase("", (byte) 0xC9);
    addCase("NZ", (byte) 0xC0);
    addCase("Z", (byte) 0xC8);
    addCase("NC", (byte) 0xD0);
    addCase("C", (byte) 0xD8);
    addCase("PO", (byte) 0xE0);
    addCase("PE", (byte) 0xE8);
    addCase("P", (byte) 0xF0);
    addCase("M", (byte) 0xF8);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "RET";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ZERO_ONE_OR_TWO;
  }
}
