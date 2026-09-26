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

package com.igormaznitsa.j2z80.translator.optimize;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.bcel.Const;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BASTORE;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.Type;

/**
 * Detects javac-shaped unrolled {@code static byte[]} fills in {@code <clinit>} so the translator
 * can emit a compact ROM template plus allocate+LDIR instead of per-element BASTORE code.
 */
public final class StaticByteArrayInitRewriter {

  public static final int MIN_LENGTH = 16;

  private StaticByteArrayInitRewriter() {
  }

  public static List<StaticByteArrayInitMatch> findMatches(final MethodGen method) {
    requireNonNull(method, "method");

    final InstructionList list = method.getInstructionList();
    if (list == null) {
      return List.of();
    }

    list.setPositions();
    final InstructionHandle[] handles = list.getInstructionHandles();
    final ConstantPoolGen constantPool = method.getConstantPool();
    final Set<InstructionHandle> branchTargets = collectBranchTargets(handles);
    final Set<InstructionHandle> exceptionBoundaries =
        collectExceptionBoundaries(method.getExceptionHandlers());

    final List<StaticByteArrayInitMatch> matches = new ArrayList<>();
    int index = 0;
    while (index < handles.length) {
      final Optional<StaticByteArrayInitMatch> match =
          tryMatchAt(handles, index, constantPool, branchTargets, exceptionBoundaries);
      if (match.isPresent()) {
        matches.add(match.get());
        index = indexOfHandle(handles, match.get().getEndHandle()) + 1;
      } else {
        index++;
      }
    }
    return List.copyOf(matches);
  }

  private static Optional<StaticByteArrayInitMatch> tryMatchAt(
      final InstructionHandle[] handles,
      final int startIndex,
      final ConstantPoolGen constantPool,
      final Set<InstructionHandle> branchTargets,
      final Set<InstructionHandle> exceptionBoundaries) {
    if (startIndex + 2 >= handles.length) {
      return Optional.empty();
    }

    final InstructionHandle lengthHandle = handles[startIndex];
    if (exceptionBoundaries.contains(lengthHandle)) {
      return Optional.empty();
    }

    final Integer length = readIntConstant(lengthHandle.getInstruction(), constantPool);
    if (length == null || length < MIN_LENGTH || length > 0xFFFF) {
      return Optional.empty();
    }

    final InstructionHandle newArrayHandle = handles[startIndex + 1];
    if (isBlockedInterior(newArrayHandle, branchTargets, exceptionBoundaries)) {
      return Optional.empty();
    }
    if (!(newArrayHandle.getInstruction() instanceof NEWARRAY)) {
      return Optional.empty();
    }
    final NEWARRAY newArray = (NEWARRAY) newArrayHandle.getInstruction();
    if (newArray.getTypecode() != Const.T_BYTE) {
      return Optional.empty();
    }

    final byte[] payload = new byte[length];
    int cursor = startIndex + 2;
    for (int elementIndex = 0; elementIndex < length; elementIndex++) {
      if (cursor + 3 >= handles.length) {
        return Optional.empty();
      }

      final InstructionHandle dupHandle = handles[cursor];
      final InstructionHandle indexHandle = handles[cursor + 1];
      final InstructionHandle valueHandle = handles[cursor + 2];
      final InstructionHandle storeHandle = handles[cursor + 3];

      if (isBlockedInterior(dupHandle, branchTargets, exceptionBoundaries)
          || isBlockedInterior(indexHandle, branchTargets, exceptionBoundaries)
          || isBlockedInterior(valueHandle, branchTargets, exceptionBoundaries)
          || isBlockedInterior(storeHandle, branchTargets, exceptionBoundaries)) {
        return Optional.empty();
      }
      if (!(dupHandle.getInstruction() instanceof DUP)) {
        return Optional.empty();
      }

      final Integer storeIndex = readIntConstant(indexHandle.getInstruction(), constantPool);
      if (storeIndex == null || storeIndex != elementIndex) {
        return Optional.empty();
      }

      final Integer storeValue = readIntConstant(valueHandle.getInstruction(), constantPool);
      if (storeValue == null) {
        return Optional.empty();
      }
      if (!(storeHandle.getInstruction() instanceof BASTORE)) {
        return Optional.empty();
      }

      payload[elementIndex] = (byte) (storeValue & 0xFF);
      cursor += 4;
    }

    if (cursor >= handles.length) {
      return Optional.empty();
    }

    final InstructionHandle putStaticHandle = handles[cursor];
    if (isBlockedInterior(putStaticHandle, branchTargets, exceptionBoundaries)) {
      return Optional.empty();
    }
    if (!(putStaticHandle.getInstruction() instanceof PUTSTATIC)) {
      return Optional.empty();
    }

    final PUTSTATIC putStatic = (PUTSTATIC) putStaticHandle.getInstruction();
    final Type fieldType = putStatic.getFieldType(constantPool);
    if (!isByteArrayType(fieldType)) {
      return Optional.empty();
    }
    if (!(putStatic.getReferenceType(constantPool) instanceof ObjectType)) {
      return Optional.empty();
    }

    final ObjectType owner = (ObjectType) putStatic.getReferenceType(constantPool);
    return Optional.of(new StaticByteArrayInitMatch(
        lengthHandle,
        putStaticHandle,
        payload,
        owner.getClassName(),
        putStatic.getFieldName(constantPool),
        fieldType));
  }

  private static boolean isByteArrayType(final Type fieldType) {
    if (!(fieldType instanceof ArrayType)) {
      return false;
    }
    return Type.BYTE.equals(((ArrayType) fieldType).getElementType());
  }

  private static boolean isBlockedInterior(
      final InstructionHandle handle,
      final Set<InstructionHandle> branchTargets,
      final Set<InstructionHandle> exceptionBoundaries) {
    return branchTargets.contains(handle) || exceptionBoundaries.contains(handle);
  }

  private static Set<InstructionHandle> collectBranchTargets(final InstructionHandle[] handles) {
    final Set<InstructionHandle> targets = new HashSet<>();
    for (final InstructionHandle handle : handles) {
      if (isBranchTarget(handle)) {
        targets.add(handle);
      }
    }
    return targets;
  }

  private static Set<InstructionHandle> collectExceptionBoundaries(
      final CodeExceptionGen[] exceptionHandlers) {
    final Set<InstructionHandle> boundaries = new HashSet<>();
    for (final CodeExceptionGen handler : exceptionHandlers) {
      boundaries.add(handler.getStartPC());
      boundaries.add(handler.getHandlerPC());
      final InstructionHandle end = handler.getEndPC();
      if (end != null) {
        boundaries.add(end);
        if (end.getNext() != null) {
          boundaries.add(end.getNext());
        }
      }
    }
    return boundaries;
  }

  private static boolean isBranchTarget(final InstructionHandle handle) {
    if (!handle.hasTargeters()) {
      return false;
    }
    for (final InstructionTargeter targeter : handle.getTargeters()) {
      if (targeter instanceof BranchInstruction) {
        return true;
      }
    }
    return false;
  }

  private static Integer readIntConstant(final Instruction instruction,
                                         final ConstantPoolGen constantPool) {
    if (instruction instanceof ICONST) {
      return ((ICONST) instruction).getValue().intValue();
    }
    if (instruction instanceof BIPUSH) {
      return ((BIPUSH) instruction).getValue().intValue();
    }
    if (instruction instanceof SIPUSH) {
      return ((SIPUSH) instruction).getValue().intValue();
    }
    if (instruction instanceof LDC) {
      final Object value = ((LDC) instruction).getValue(constantPool);
      if (value instanceof Integer) {
        return (Integer) value;
      }
    }
    return null;
  }

  private static int indexOfHandle(final InstructionHandle[] handles,
                                   final InstructionHandle target) {
    for (int index = 0; index < handles.length; index++) {
      if (handles[index] == target) {
        return index;
      }
    }
    throw new IllegalStateException("Matched handle is not in the instruction list");
  }
}
