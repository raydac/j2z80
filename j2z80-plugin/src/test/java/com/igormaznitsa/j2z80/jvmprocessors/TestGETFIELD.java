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

import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.z80asm.Z80Asm;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.Type;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestGETFIELD extends AbstractJvmCommandProcessorTest {

  private final static String CLASS_NAME = "some.class.test.tttt";
  private final static String FIELD_NAME = "Field";
  private final static Type FIELD_TYPE = Type.INT;

  private final static int CP_INDEX_CLASS_NAME = CONSTANT_USER_DEFINED + 1;
  private final static int CP_INDEX_FIELD_NAME = CONSTANT_USER_DEFINED + 2;
  private final static int CP_INDEX_FIELD_NAMEANDTYPE = CONSTANT_USER_DEFINED + 3;
  private final static int CP_INDEX_CLASSREF = CONSTANT_USER_DEFINED + 4;
  private final static int CP_INDEX_FIELDREF = CONSTANT_USER_DEFINED + 5;
  private final static int CP_INDEX_FIELD_SIGNATURE = CONSTANT_USER_DEFINED + 6;

  private final static int VALUE = 0xCAFE;
  private final static int OBJECT_ADDRESS = 0x8000;
  private final static int FIELD_OFFSET = 1103;

  @Test(timeout = 3000L)
  public void testExecution() throws IOException {
    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(GETFIELD.class);
    final StringWriter writer = new StringWriter();


    final ConstantUtf8 cp_className = new ConstantUtf8(CLASS_NAME);
    final ConstantUtf8 cp_fieldName = new ConstantUtf8(FIELD_NAME);
    final ConstantClass cp_class = new ConstantClass(CP_INDEX_CLASS_NAME);
    final ConstantNameAndType cp_nameandtype = new ConstantNameAndType(CP_INDEX_FIELD_NAME, CP_INDEX_FIELD_SIGNATURE);
    final ConstantUtf8 cp_fieldtype = new ConstantUtf8(FIELD_TYPE.getSignature());
    final ConstantFieldref cp_fieldref = new ConstantFieldref(CP_INDEX_CLASSREF, CP_INDEX_FIELD_NAMEANDTYPE);

    when(CP_GEN_MOCK.getConstant(CP_INDEX_CLASSREF)).thenReturn(cp_class);
    when(CP_GEN_MOCK.getConstant(CP_INDEX_CLASS_NAME)).thenReturn(cp_className);
    when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELDREF)).thenReturn(cp_fieldref);
    when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_NAME)).thenReturn(cp_fieldName);
    when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_NAMEANDTYPE)).thenReturn(cp_nameandtype);
    when(CP_GEN_MOCK.getConstant(CP_INDEX_FIELD_SIGNATURE)).thenReturn(cp_fieldtype);

    when(CP_MOCK.getConstantString(CP_INDEX_CLASSREF, Const.CONSTANT_Class)).thenReturn(CLASS_NAME);

    final String staticFieldLabel = LabelAndFrameUtils.makeLabelNameForField(CLASS_NAME, FIELD_NAME, FIELD_TYPE);

    final GETFIELD testInstruction = new GETFIELD(CP_INDEX_FIELDREF);

    processor.process(CLASS_PROCESSOR_MOCK, testInstruction, mock(InstructionHandle.class), writer);

    pokew(OBJECT_ADDRESS + FIELD_OFFSET, VALUE);

    push(OBJECT_ADDRESS);
    final Z80Asm asm = assertLinearExecutionToEnd(writer.toString());
    assertEquals(VALUE, pop());
    assertStackEmpty();

    assertEquals(VALUE, peekw(OBJECT_ADDRESS + FIELD_OFFSET));
  }

  @Override
  public String getAsmPostfix() {
    final String fieldOffset = LabelAndFrameUtils.makeLabelNameForFieldOffset(CLASS_NAME, FIELD_NAME, FIELD_TYPE);
    return fieldOffset + ": EQU " + FIELD_OFFSET;
  }


}
