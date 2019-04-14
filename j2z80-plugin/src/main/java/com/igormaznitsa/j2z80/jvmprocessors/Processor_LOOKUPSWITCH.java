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
package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LOOKUPSWITCH;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

// class to process LOOKUPSWITCH with code 171
public class Processor_LOOKUPSWITCH extends AbstractJvmCommandProcessor {


  private final String template;

  public Processor_LOOKUPSWITCH() {
    super();
    template = loadResourceFileAsString("LOOKUPSWITCH.a80");
  }

  @Override
  public String getName() {
    return "LOOKUPSWITCH";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final LOOKUPSWITCH lookupswitch = (LOOKUPSWITCH) instruction;

    final int[] matchs = lookupswitch.getMatchs();
    final InstructionHandle[] targets = lookupswitch.getTargets();

    final InstructionHandle defaultTarget = lookupswitch.getTarget();
    final String defaultJump = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), defaultTarget.getPosition());

    out.write(template
        .replace(MACROS_ADDRESS, defaultJump)
        .replace(MACROS_VALUE, Integer.toString(targets.length))
    );

    if (matchs.length > 0) {
      for (int branchIndex = 0; branchIndex < matchs.length; branchIndex++) {
        final int match = matchs[branchIndex];
        final InstructionHandle target = targets[branchIndex];
        final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), target.getPosition());

        out.write("DEFW #" + Integer.toHexString(match & 0xFFFF).toUpperCase(Locale.ENGLISH) + "\n");
        out.write("DEFW " + jumpLabel + "\n");
      }
    }

    out.write(NEXT_LINE);
  }
}
