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

package com.igormaznitsa.j2z80.translator.optimize;

import static java.util.Objects.requireNonNull;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;

/**
 * A contiguous {@code byte[]} fill in {@code <clinit>} that can be emitted as a ROM template.
 */
public record StaticByteArrayInitMatch(InstructionHandle startHandle, InstructionHandle endHandle,
                                       byte[] payload, String className, String fieldName,
                                       Type fieldType) {

  public StaticByteArrayInitMatch(
      final InstructionHandle startHandle,
      final InstructionHandle endHandle,
      final byte[] payload,
      final String className,
      final String fieldName,
      final Type fieldType) {
    this.startHandle = requireNonNull(startHandle, "startHandle");
    this.endHandle = requireNonNull(endHandle, "endHandle");
    this.payload = requireNonNull(payload, "payload").clone();
    this.className = requireNonNull(className, "className");
    this.fieldName = requireNonNull(fieldName, "fieldName");
    this.fieldType = requireNonNull(fieldType, "fieldType");
  }

  @Override
  public byte[] payload() {
    return this.payload.clone();
  }

  public int getLength() {
    return this.payload.length;
  }
}
