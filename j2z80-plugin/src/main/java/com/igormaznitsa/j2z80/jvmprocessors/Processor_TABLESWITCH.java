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
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.TABLESWITCH;

// class to process TABLESWITCH with code 170
public class Processor_TABLESWITCH extends AbstractJvmCommandProcessor {

  private static final String MACROS_LOW_IDEX = "%lowindex%";
  private static final String MACROS_HIGH_IDEX = "%highindex%";
  private static final String MACROS_DEFAULT = "%default%";

  private final String template;

  public Processor_TABLESWITCH() {
    super();
    template = loadResourceFileAsString("TABLESWITCH.a80");
  }

  @Override
  public String getName() {
    return "TABLESWITCH";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction,
                      final InstructionHandle handle,
                      ClassLoader bootstrapClassLoader, final Writer out) throws IOException {
    final TABLESWITCH tableswitch = (TABLESWITCH) handle.getInstruction();

    final int[] matchs = tableswitch.getMatchs();
    final InstructionHandle[] targets = tableswitch.getTargets();

    final InstructionHandle defaultTarget = tableswitch.getTarget();
    final String defaultJump = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), defaultTarget.getPosition());

    final int lowIndex = matchs[0];
    final int highIndex = matchs[matchs.length - 1];

    out.write(template
        .replace(MACROS_LOW_IDEX, Integer.toString(lowIndex))
        .replace(MACROS_HIGH_IDEX, Integer.toString(highIndex))
        .replace(MACROS_DEFAULT, defaultJump)
    );

    for (int branchIndex = 0; branchIndex < matchs.length; branchIndex++) {
      final InstructionHandle target = targets[branchIndex];
      final String jumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(methodTranslator.getMethod(), target.getPosition());

      out.write("DEFW " + jumpLabel + "\n");
    }

    out.write(NEXT_LINE);
  }
}
