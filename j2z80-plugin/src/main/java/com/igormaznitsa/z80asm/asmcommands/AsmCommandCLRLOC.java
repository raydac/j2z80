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

public class AsmCommandCLRLOC extends AbstractAsmCommand {

  public static final String NAME = "CLRLOC";

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    context.clearLocalLabels();
    return EMPTY_ARRAY;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.ANY;
  }

  @Override
  public boolean isSpecialDirective() {
    return true;
  }
}
