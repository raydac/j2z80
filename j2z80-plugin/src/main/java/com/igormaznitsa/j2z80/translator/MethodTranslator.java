/* 
 * Copyright 2012-2026 Igor Maznitsa.
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

import static com.igormaznitsa.j2z80.translator.utils.MethodUtils.isStaticInitializer;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import com.igormaznitsa.j2z80.translator.optimize.StaticByteArrayInitMatch;
import com.igormaznitsa.j2z80.translator.optimize.StaticByteArrayInitRewriter;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.MethodGen;

/**
 * The class is a method translator. It translates a parsed class method into Z80 assembler.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MethodTranslator {

  private final TranslatorContext translatorContext;
  private final ClassMethodInfo method;

  public MethodTranslator(final TranslatorContext context, final ClassMethodInfo method) {
    this.translatorContext = context;
    this.method = method;
  }

  public TranslatorContext getTranslatorContext() {
    return this.translatorContext;
  }

  public String[] translate(final ClassLoader bootstrapClassLoader) throws IOException {
    final List<String> asm = this.method2asm(bootstrapClassLoader);
    final List<String> result = new ArrayList<>();
    for (final String str : asm) {
      result.addAll(Arrays.asList(Utils.breakToLines(str)));
    }

    return result.toArray(new String[0]);
  }

  public ClassMethodInfo getMethod() {
    return this.method;
  }

  private List<String> method2asm(final ClassLoader bootstrapClassLoader) throws IOException {
    final List<String> result = new ArrayList<>();
    result.add(LabelAndFrameUtils.makeLabelNameForMethod(this.method) + ':');

    final MethodGen methodG = this.method.getMethodGen();
    CheckedExceptionSupport.validateMethodExceptions(this.translatorContext, methodG);
    if (CheckedExceptionSupport.declaresCheckedExceptions(methodG)) {
      result.add(CheckedExceptionSupport.clearPendingException());
    }

    final InstructionList list = methodG.getInstructionList();
    list.setPositions();
    final InstructionHandle[] handles = list.getInstructionHandles();
    final CodeExceptionGen[] exceptionHandlers = methodG.getExceptionHandlers();

    final CompactedInits compactedInits = this.prepareCompactedByteArrayInits(methodG);

    for (final InstructionHandle handler : handles) {
      if (compactedInits.skipHandles.contains(handler)) {
        continue;
      }

      if (this.isLabeled(handler, exceptionHandlers)) {
        final String methodJumpLabel =
            LabelAndFrameUtils.makeClassMethodJumpLabel(this.method.getClassInfo(),
                this.method.getMethodGen(), handler.getPosition());
        result.add(methodJumpLabel + ":\n");
      }

      final CompactedInit compacted = compactedInits.starts.get(handler);
      if (compacted != null) {
        result.add(this.emitCompactedByteArrayInit(compacted));
        continue;
      }

      final Instruction instruction = handler.getInstruction();
      final AbstractJvmCommandProcessor processor =
          AbstractJvmCommandProcessor.findProcessor(instruction.getClass());

      if (processor == null) {
        throw new UnsupportedOperationException(
            "J2Z80 doesn't support JVM instruction: " + instruction.getName());
      }

      this.getTranslatorContext().registerAdditionsUsedByClass(processor.getClass());

      final StringWriter writer = new StringWriter(256);
      try {
        processor.process(this, instruction, handler, bootstrapClassLoader, writer);
      } catch (IllegalArgumentException ex) {
        this.getTranslatorContext().getLogger()
            .logError(this.method + " [" + ex.getMessage() + ']');
        throw ex;
      }

      result.add(writer.toString());
    }

    return result;
  }

  private CompactedInits prepareCompactedByteArrayInits(final MethodGen methodG) {
    if (!isStaticInitializer(methodG.getMethod())) {
      return CompactedInits.empty();
    }

    final List<StaticByteArrayInitMatch> matches = StaticByteArrayInitRewriter.findMatches(methodG);
    if (matches.isEmpty()) {
      return CompactedInits.empty();
    }

    final Map<InstructionHandle, CompactedInit> starts = new HashMap<>();
    final Set<InstructionHandle> skipHandles = new HashSet<>();

    for (final StaticByteArrayInitMatch match : matches) {
      final String templateLabel =
          this.translatorContext.registerStaticByteArrayTemplate(match.getPayload());
      this.translatorContext.getLogger().logInfo(
          "Compacted static byte[] " + match.getClassName() + '#' + match.getFieldName()
              + " (" + match.getLength() + " elements, ROM-resident)");

      starts.put(match.getStartHandle(), new CompactedInit(match, templateLabel));

      InstructionHandle cursor = match.getStartHandle().getNext();
      while (cursor != null) {
        skipHandles.add(cursor);
        if (cursor == match.getEndHandle()) {
          break;
        }
        cursor = cursor.getNext();
      }
    }

    return new CompactedInits(starts, skipHandles);
  }

  private String emitCompactedByteArrayInit(final CompactedInit compacted) {
    final StaticByteArrayInitMatch match = compacted.match;
    final String fieldLabel = LabelAndFrameUtils.makeLabelNameForField(
        match.getClassName(), match.getFieldName(), match.getFieldType());

    return "    LD BC," + compacted.templateLabel + AbstractJvmCommandProcessor.NEXT_LINE
        + "    LD (" + fieldLabel + "),BC" + AbstractJvmCommandProcessor.NEXT_LINE;
  }

  private boolean isLabeled(final InstructionHandle handle,
                            final CodeExceptionGen[] exceptionHandlers) {
    if (handle.hasTargeters()) {
      for (final InstructionTargeter targeter : handle.getTargeters()) {
        if (targeter instanceof Instruction) {
          return true;
        }
      }
    }
    for (final CodeExceptionGen exceptionHandler : exceptionHandlers) {
      if (handle == exceptionHandler.getStartPC()
          || handle == exceptionHandler.getHandlerPC()
          || handle == this.exclusiveEnd(exceptionHandler)) {
        return true;
      }
    }
    return false;
  }

  private InstructionHandle exclusiveEnd(final CodeExceptionGen exceptionHandler) {
    final InstructionHandle end = exceptionHandler.getEndPC();
    return end == null ? null : end.getNext();
  }

  public ConstantPoolGen getConstantPool() {
    return this.method.getClassInfo().getConstantPool();
  }

  public String registerUsedConstantPoolItem(final int itemIndex) {
    final Constant item = getConstantPool().getConstant(itemIndex);
    final String result;
    if (item instanceof ConstantString) {
      final ConstantUtf8 utfConst =
          (ConstantUtf8) getConstantPool().getConstant(((ConstantString) item).getStringIndex());
      result = LabelAndFrameUtils.makeLabelForConstantPoolItem(this.method.getClassInfo(),
          ((ConstantString) item).getStringIndex());
      getTranslatorContext().registerConstantPoolItem(result, utfConst);
    } else {
      result =
          LabelAndFrameUtils.makeLabelForConstantPoolItem(this.method.getClassInfo(), itemIndex);
      getTranslatorContext().registerConstantPoolItem(result, item);
    }
    return result;
  }

  private static final class CompactedInits {
    private final Map<InstructionHandle, CompactedInit> starts;
    private final Set<InstructionHandle> skipHandles;

    private CompactedInits(final Map<InstructionHandle, CompactedInit> starts,
                           final Set<InstructionHandle> skipHandles) {
      this.starts = starts;
      this.skipHandles = skipHandles;
    }

    private static CompactedInits empty() {
      return new CompactedInits(Map.of(), Set.of());
    }
  }

  private static final class CompactedInit {
    private final StaticByteArrayInitMatch match;
    private final String templateLabel;

    private CompactedInit(final StaticByteArrayInitMatch match, final String templateLabel) {
      this.match = match;
      this.templateLabel = templateLabel;
    }
  }

}
