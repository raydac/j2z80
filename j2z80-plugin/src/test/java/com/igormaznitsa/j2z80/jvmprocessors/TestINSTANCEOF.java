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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.InstanceofTable;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Before;
import org.junit.Test;

public class TestINSTANCEOF extends AbstractTestBasedOnMemoryManager implements NeedsInstanceofManager {
  private static final String TEST_CLASS_NAME1 = "test.instanceof.class1";
  private static final Integer TEST_CLASS_NAME1_ID = Integer.valueOf(0x101);

  private static final String TEST_CLASS_NAME2 = "test.instanceof.class2";
  private static final Integer TEST_CLASS_NAME2_ID = Integer.valueOf(0x102);

  private static final String TEST_CLASS_NAME3 = "test.instanceof.class3";
  private static final Integer TEST_CLASS_NAME3_ID = Integer.valueOf(0x103);

  private static final String TEST_CLASS_NAME4 = "test.instanceof.class4";
  private static final Integer TEST_CLASS_NAME4_ID = Integer.valueOf(0x104);

  private static final String TEST_CLASS_NAME5 = "test.instanceof.class5";
  private static final Integer TEST_CLASS_NAME5_ID = Integer.valueOf(0x105);

  private static final String TEST_CLASS_NAME6 = "test.instanceof.class6";
  private static final Integer TEST_CLASS_NAME6_ID = Integer.valueOf(0x106);

  private static final int CLASS_NAME_INDEX = CONSTANT_USER_DEFINED + 1;
  private static final int CLASS_INDEX = CONSTANT_USER_DEFINED + 2;

  private static final int CLASS_FIELD_NUMBER = 32;

  private String asmText;

  @Before
  public void prepareTest() {
    final ConstantUtf8 cpClassName = new ConstantUtf8(TEST_CLASS_NAME1);
    final ConstantClass cpClass = new ConstantClass(CLASS_NAME_INDEX);
    when(CP_GEN_MOCK.getConstant(CLASS_NAME_INDEX)).thenReturn(cpClassName);
    when(CP_GEN_MOCK.getConstant(CLASS_INDEX)).thenReturn(cpClass);
    when(CP_MOCK.getConstantString(CLASS_INDEX, Const.CONSTANT_Class)).thenReturn(TEST_CLASS_NAME1);

    final ClassID cid1 = new ClassID(TEST_CLASS_NAME1);
    final ClassID cid2 = new ClassID(TEST_CLASS_NAME2);
    final ClassID cid3 = new ClassID(TEST_CLASS_NAME3);
    final ClassID cid4 = new ClassID(TEST_CLASS_NAME4);
    final ClassID cid5 = new ClassID(TEST_CLASS_NAME5);
    final ClassID cid6 = new ClassID(TEST_CLASS_NAME6);

    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid1))).thenReturn(TEST_CLASS_NAME1_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid2))).thenReturn(TEST_CLASS_NAME2_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid3))).thenReturn(TEST_CLASS_NAME3_ID);

    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid4))).thenReturn(TEST_CLASS_NAME4_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid5))).thenReturn(TEST_CLASS_NAME5_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid6))).thenReturn(TEST_CLASS_NAME6_ID);

    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid1))).thenReturn(TEST_CLASS_NAME1_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid2))).thenReturn(TEST_CLASS_NAME2_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid3))).thenReturn(TEST_CLASS_NAME3_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid4))).thenReturn(TEST_CLASS_NAME4_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid5))).thenReturn(TEST_CLASS_NAME5_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid6))).thenReturn(TEST_CLASS_NAME6_ID);

  }

  private void prepareTabe(final TestingState state) {
    final StringBuilder bldr = new StringBuilder();

    final InstanceofTable table = new InstanceofTable(TRANSLATOR_MOCK, new HashSet<ClassID>());

    switch (state) {
      case SUCESSOR: {
        final InstanceofTable.RowInstance r1 = table.addRow(new ClassID(TEST_CLASS_NAME4));
        r1.addClass(new ClassID(TEST_CLASS_NAME5));
        r1.addClass(new ClassID(TEST_CLASS_NAME6));

        final InstanceofTable.RowInstance r = table.addRow(new ClassID(TEST_CLASS_NAME1));
        r.addClass(new ClassID(TEST_CLASS_NAME2));
        r.addClass(new ClassID(TEST_CLASS_NAME3));
      }
      break;
      case NOT_SUCCESSOR: {
        final InstanceofTable.RowInstance r1 = table.addRow(new ClassID(TEST_CLASS_NAME4));
        r1.addClass(new ClassID(TEST_CLASS_NAME5));
        r1.addClass(new ClassID(TEST_CLASS_NAME6));
        r1.addClass(new ClassID(TEST_CLASS_NAME3));

        final InstanceofTable.RowInstance r = table.addRow(new ClassID(TEST_CLASS_NAME1));
        r.addClass(new ClassID(TEST_CLASS_NAME2));
      }
      break;
      case NOT_IN_LIST: {
        final InstanceofTable.RowInstance r1 = table.addRow(new ClassID(TEST_CLASS_NAME4));
        r1.addClass(new ClassID(TEST_CLASS_NAME5));
        r1.addClass(new ClassID(TEST_CLASS_NAME6));

        final InstanceofTable.RowInstance r = table.addRow(new ClassID(TEST_CLASS_NAME1));
        r.addClass(new ClassID(TEST_CLASS_NAME2));
      }
      break;
      default:
        throw new IllegalArgumentException("Unsupported state");
    }


    // add class ids
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME1))).append(": EQU ").append(TEST_CLASS_NAME1_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME2))).append(": EQU ").append(TEST_CLASS_NAME2_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3))).append(": EQU ").append(TEST_CLASS_NAME3_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME4))).append(": EQU ").append(TEST_CLASS_NAME4_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME5))).append(": EQU ").append(TEST_CLASS_NAME5_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME6))).append(": EQU ").append(TEST_CLASS_NAME6_ID.intValue()).append('\n');

    asmText = table.toAsm() + "\n";

    asmText += bldr.toString();
  }

  @Test(timeout = 3000L)
  public void testClassIsSuccessor() throws IOException {
    prepareTabe(TestingState.SUCESSOR);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INSTANCEOF.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new INSTANCEOF(CLASS_INDEX), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString(), (CLASS_FIELD_NUMBER << 1) + 4);
    final int stacktop = pop();
    assertStackEmpty();
    assertEquals("The class must be instanceof for the #2211", 1, stacktop);
  }

  @Test(timeout = 3000L)
  public void testClassIsNotSuccessor() throws IOException {
    prepareTabe(TestingState.NOT_SUCCESSOR);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INSTANCEOF.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new INSTANCEOF(CLASS_INDEX), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString(), (CLASS_FIELD_NUMBER << 1) + 4);
    final int stacktop = pop();
    assertStackEmpty();
    assertEquals("The class must be false because the class is not marked as a compatible one", 0, stacktop);
  }

  @Test(timeout = 3000L)
  public void testClassIsNotInList() throws IOException {
    prepareTabe(TestingState.NOT_IN_LIST);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(INSTANCEOF.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new INSTANCEOF(CLASS_INDEX), mock(InstructionHandle.class), writer);
    assertLinearExecutionToEnd(writer.toString(), (CLASS_FIELD_NUMBER << 1) + 4);
    final int stacktop = pop();
    assertStackEmpty();
    assertEquals("Must be false because the class is not at list at all", 0, stacktop);
  }

  @Override
  public String getAsmPrefix() {
    return "LD BC, " + CLASS_FIELD_NUMBER + "\n"
        + "LD DE," + LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3)) + "\n"
        + "CALL " + SUB_ALLOCATE_OBJECT + "\n"
        + "PUSH BC\n";
  }

  @Override
  public String prepareMemoryManagerText() {
    return INSTANCEOFMANAGER_TEXT.replace(MACRO_INSTANCEOFTABLE, asmText) + "\n" + super.prepareMemoryManagerText();
  }

  private enum TestingState {
    SUCESSOR,
    NOT_SUCCESSOR,
    NOT_IN_LIST
  }

}
