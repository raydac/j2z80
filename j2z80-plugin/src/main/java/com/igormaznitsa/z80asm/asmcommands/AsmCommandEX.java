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

public class AsmCommandEX extends AbstractAsmCommand {

  public AsmCommandEX() {
    super();
    addCase("DE,HL", (byte) 0xEB);
    addCase("(SP),HL", (byte) 0xE3);
    addCase("(SP),IX", (byte) 0xDD, (byte) 0xE3);
    addCase("(SP),IY", (byte) 0xFD, (byte) 0xE3);
    addCase("AF,AF'", (byte) 0x08);
  }

  @Override
  public byte[] makeMachineCode(AsmTranslator context, final ParsedAsmLine asm) {
    return getPatternCase(asm.getSignature());
  }

  @Override
  public String getName() {
    return "EX";
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }

}
