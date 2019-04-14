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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class is a method translator. It translates a parsed class method into Z80 assembler.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MethodTranslator {

  private final TranslatorContext translatorContext;
  private final ClassMethodInfo theMethod;

  public MethodTranslator(final TranslatorContext context, final ClassMethodInfo method) {
    this.translatorContext = context;
    this.theMethod = method;
  }

  public TranslatorContext getTranslatorContext() {
    return this.translatorContext;
  }

  public String[] translate() throws IOException {
    final List<String> asm = methodToAsm();
    final List<String> result = new ArrayList<>();
    for (final String str : asm) {
      final String[] splitted = Utils.breakToLines(str);
      result.addAll(Arrays.asList(splitted));
    }

    return result.toArray(new String[0]);
  }

  public ClassMethodInfo getMethod() {
    return theMethod;
  }

  private List<String> methodToAsm() throws IOException {
    final List<String> result = new ArrayList<>(1024);

    result.add(LabelAndFrameUtils.makeLabelNameForMethod(theMethod) + ':');

    final MethodGen methodG = theMethod.getMethodGen();

    final InstructionList list = methodG.getInstructionList();
    list.setPositions();
    final InstructionHandle[] handles = list.getInstructionHandles();

    for (final InstructionHandle handler : handles) {
      final Instruction instruction = handler.getInstruction();

      final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());

      if (processor == null) {
        throw new IllegalArgumentException("Unsupported instruction detected [" + instruction.getName() + ']');
      }

      getTranslatorContext().registerAdditionsUsedByClass(processor.getClass());

      final StringWriter writer = new StringWriter(256);
      try {
        processor.process(this, instruction, handler, writer);
      } catch (IllegalArgumentException ex) {
        getTranslatorContext().getLogger().logError(theMethod.toString() + " [" + ex.getMessage() + ']');
        throw ex;
      }

      if (checkHandleForInstructionTargeters(handler)) {
        // the instruction is a jump target so we label it
        final String methodJumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(theMethod.getClassInfo(), theMethod.getMethodGen(), handler.getPosition());
        result.add(methodJumpLabel + ":\r\n");
      }

      result.add(writer.toString());
    }

    return result;
  }

  private boolean checkHandleForInstructionTargeters(final InstructionHandle handle) {
    if (handle.hasTargeters()) {
      for (final InstructionTargeter targeter : handle.getTargeters()) {
        if (targeter instanceof Instruction) {
          return true;
        }
      }
    }
    return false;
  }

  public ConstantPoolGen getConstantPool() {
    return theMethod.getClassInfo().getConstantPool();
  }

  public String registerUsedConstantPoolItem(final int itemIndex) {
    final Constant item = getConstantPool().getConstant(itemIndex);

    String result;

    if (item instanceof ConstantString) {
      final ConstantUtf8 utfconst = (ConstantUtf8) getConstantPool().getConstant(((ConstantString) item).getStringIndex());
      result = LabelAndFrameUtils.makeLabelForConstantPoolItem(theMethod.getClassInfo(), ((ConstantString) item).getStringIndex());
      getTranslatorContext().registerConstantPoolItem(result, utfconst);
    } else {
      result = LabelAndFrameUtils.makeLabelForConstantPoolItem(theMethod.getClassInfo(), itemIndex);
      getTranslatorContext().registerConstantPoolItem(result, item);
    }
    return result;
  }

}
