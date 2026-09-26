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

import static org.apache.bcel.Const.ACC_FINAL;
import static org.apache.bcel.Const.ACC_PUBLIC;
import static org.apache.bcel.Const.ACC_STATIC;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.junit.Test;

public class StaticByteArrayInitRewriterTest {

  @Test
  public void detectsContiguousFillOfThresholdLength() {
    final byte[] payload = this.payload(32);
    final MethodGen clinit = this.buildClinit(payload);

    final List<StaticByteArrayInitMatch> matches = StaticByteArrayInitRewriter.findMatches(clinit);

    assertEquals(1, matches.size());
    assertEquals("demo.Data", matches.get(0).getClassName());
    assertEquals("DATA", matches.get(0).getFieldName());
    assertArrayEquals(payload, matches.get(0).getPayload());
  }

  @Test
  public void ignoresFillsBelowThreshold() {
    final MethodGen clinit =
        this.buildClinit(this.payload(StaticByteArrayInitRewriter.MIN_LENGTH - 1));

    assertTrue(StaticByteArrayInitRewriter.findMatches(clinit).isEmpty());
  }

  @Test
  public void ignoresSparseIndexOrder() {
    final ClassGen classGen = this.newClass();
    final ConstantPoolGen constantPool = classGen.getConstantPool();
    final InstructionFactory factory = new InstructionFactory(classGen, constantPool);
    final InstructionList list = new InstructionList();
    final ArrayType byteArray = new ArrayType(Type.BYTE, 1);

    list.append(new PUSH(constantPool, 16));
    list.append(factory.createNewArray(Type.BYTE, (short) 1));
    for (int index = 0; index < 16; index++) {
      list.append(InstructionConst.DUP);
      list.append(new PUSH(constantPool, index == 3 ? 5 : index));
      list.append(new BIPUSH((byte) index));
      list.append(InstructionConst.BASTORE);
    }
    list.append(factory.createPutStatic("demo.Data", "DATA", byteArray));
    list.append(InstructionConst.RETURN);

    final MethodGen clinit = this.clinitMethod(classGen, list);
    assertTrue(StaticByteArrayInitRewriter.findMatches(clinit).isEmpty());
  }

  @Test
  public void ignoresInterruptedFill() {
    final ClassGen classGen = this.newClass();
    final ConstantPoolGen constantPool = classGen.getConstantPool();
    final InstructionFactory factory = new InstructionFactory(classGen, constantPool);
    final InstructionList list = new InstructionList();
    final ArrayType byteArray = new ArrayType(Type.BYTE, 1);

    list.append(new PUSH(constantPool, 16));
    list.append(factory.createNewArray(Type.BYTE, (short) 1));
    for (int index = 0; index < 8; index++) {
      list.append(InstructionConst.DUP);
      list.append(new PUSH(constantPool, index));
      list.append(new BIPUSH((byte) index));
      list.append(InstructionConst.BASTORE);
    }
    list.append(InstructionConst.NOP);
    for (int index = 8; index < 16; index++) {
      list.append(InstructionConst.DUP);
      list.append(new PUSH(constantPool, index));
      list.append(new BIPUSH((byte) index));
      list.append(InstructionConst.BASTORE);
    }
    list.append(factory.createPutStatic("demo.Data", "DATA", byteArray));
    list.append(InstructionConst.RETURN);

    final MethodGen clinit = this.clinitMethod(classGen, list);
    assertTrue(StaticByteArrayInitRewriter.findMatches(clinit).isEmpty());
  }

  private MethodGen buildClinit(final byte[] payload) {
    final ClassGen classGen = this.newClass();
    final ConstantPoolGen constantPool = classGen.getConstantPool();
    final InstructionFactory factory = new InstructionFactory(classGen, constantPool);
    final InstructionList list = new InstructionList();
    final ArrayType byteArray = new ArrayType(Type.BYTE, 1);

    list.append(new PUSH(constantPool, payload.length));
    list.append(factory.createNewArray(Type.BYTE, (short) 1));
    for (int index = 0; index < payload.length; index++) {
      list.append(InstructionConst.DUP);
      list.append(new PUSH(constantPool, index));
      list.append(new BIPUSH(payload[index]));
      list.append(InstructionConst.BASTORE);
    }
    list.append(factory.createPutStatic("demo.Data", "DATA", byteArray));
    list.append(InstructionConst.RETURN);

    return this.clinitMethod(classGen, list);
  }

  private MethodGen clinitMethod(final ClassGen classGen, final InstructionList list) {
    final MethodGen method = new MethodGen(
        ACC_STATIC,
        Type.VOID,
        Type.NO_ARGS,
        new String[0],
        "<clinit>",
        classGen.getClassName(),
        list,
        classGen.getConstantPool());
    method.setMaxStack();
    method.setMaxLocals();
    return method;
  }

  private ClassGen newClass() {
    final ClassGen classGen = new ClassGen(
        "demo.Data",
        "java.lang.Object",
        "<generated>",
        ACC_PUBLIC,
        null);
    final FieldGen field = new FieldGen(
        ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
        new ArrayType(Type.BYTE, 1),
        "DATA",
        classGen.getConstantPool());
    classGen.addField(field.getField());
    return classGen;
  }

  private byte[] payload(final int length) {
    final byte[] data = new byte[length];
    for (int index = 0; index < length; index++) {
      data[index] = (byte) (index * 3);
    }
    return data;
  }
}
