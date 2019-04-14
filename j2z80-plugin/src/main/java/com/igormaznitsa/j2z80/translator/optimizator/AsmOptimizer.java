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
package com.igormaznitsa.j2z80.translator.optimizator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.List;

/**
 * The interface describes an optimizer working on the assembler level
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface AsmOptimizer {
  /**
   * Optimize assembler text.
   *
   * @param context        a translator context, must not be null
   * @param asmParsedLines a list contains parsed assembler lines without spaces and nulls
   * @return a list contains optimized assembler lines
   */
  List<ParsedAsmLine> optimizeAsmText(TranslatorContext context, List<ParsedAsmLine> asmParsedLines);
}
