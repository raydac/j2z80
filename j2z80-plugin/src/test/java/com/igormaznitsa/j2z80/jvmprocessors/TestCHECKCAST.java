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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.InstanceofTable;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Before;
import org.junit.Test;

public class TestCHECKCAST extends AbstractTestBasedOnMemoryManager implements NeedsInstanceofManager {

  protected static final String CHECKCAST_TEXT;
  protected static final String ATHROWMANAGER_TEXT;

  private static final int EXCEPTION_FLAG_ADDRESS = 0x100;
  private static final int EXCEPTION_FLAG = 0xFF;
  private static final String TEST_CLASS_NAME1 = "test.instanceof.class1";
  private static final Integer TEST_CLASS_NAME1_ID = Integer.valueOf(0x101);
  private static final String TEST_CLASS_NAME2 = "test.instanceof.class2";
  private static final Integer TEST_CLASS_NAME2_ID = Integer.valueOf(0x102);
  private static final String TEST_CLASS_NAME3 = "test.instanceof.class3";
  private static final Integer TEST_CLASS_NAME3_ID = Integer.valueOf(0x103);
  private static final int CLASS_NAME_INDEX = CONSTANT_USER_DEFINED + 1;
  private static final int CLASS_INDEX = CONSTANT_USER_DEFINED + 2;
  private static final int CLASS_FIELD_NUMBER = 32;

  static {
    try {
      CHECKCAST_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "CHECKCAST_MANAGER.a80");
    } catch (IOException ex) {
      throw new Error("Can't read checkcast manager assembler text");
    }
    try {
      ATHROWMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
    } catch (IOException ex) {
      throw new Error("Can't read athrow manager assembler text");
    }
  }

  private String postfixText;

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

    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid1))).thenReturn(TEST_CLASS_NAME1_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid2))).thenReturn(TEST_CLASS_NAME2_ID);
    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid3))).thenReturn(TEST_CLASS_NAME3_ID);

    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid1))).thenReturn(TEST_CLASS_NAME1_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid2))).thenReturn(TEST_CLASS_NAME2_ID);
    when(TRANSLATOR_MOCK.registerClassForCastCheck(eq(cid3))).thenReturn(TEST_CLASS_NAME3_ID);
  }

  private void prepareTabe(final boolean successor) {
    final StringBuilder bldr = new StringBuilder();

    final InstanceofTable table = new InstanceofTable(TRANSLATOR_MOCK, new HashSet<ClassID>());
    if (successor) {
      final InstanceofTable.RowInstance r = table.addRow(new ClassID(TEST_CLASS_NAME1));
      r.addClass(new ClassID(TEST_CLASS_NAME2));
      r.addClass(new ClassID(TEST_CLASS_NAME3));
    } else {
      final InstanceofTable.RowInstance r = table.addRow(new ClassID(TEST_CLASS_NAME1));
      r.addClass(new ClassID(TEST_CLASS_NAME2));
    }

    // add class ids
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME1))).append(": EQU ").append(TEST_CLASS_NAME1_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME2))).append(": EQU ").append(TEST_CLASS_NAME2_ID.intValue()).append('\n');
    bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3))).append(": EQU ").append(TEST_CLASS_NAME3_ID.intValue()).append('\n');

    postfixText = table.toAsm() + "\n";

    postfixText += bldr.toString();
  }

  @Test(timeout = 3000L)
  public void testClassIsSuccessor() throws IOException {
    prepareTabe(true);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(CHECKCAST.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new CHECKCAST(CLASS_INDEX),
        mock(InstructionHandle.class), this.getClass().getClassLoader(),
        writer);
    assertLinearExecutionToEnd(writer.toString(), (CLASS_FIELD_NUMBER << 1) + 4);
    final int stacktop = pop();
    assertStackEmpty();
    assertEquals("The class must be instanceof for the #2211", getInitialMemoryAddress() + 4, stacktop);
  }

  @Test(timeout = 3000L)
  public void testClassIsNotSuccessor() throws IOException {
    prepareTabe(false);

    final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(CHECKCAST.class);
    final StringWriter writer = new StringWriter();

    processor.process(CLASS_PROCESSOR_MOCK, new CHECKCAST(CLASS_INDEX),
        mock(InstructionHandle.class), this.getClass().getClassLoader(),
        writer);
    final Z80Asm asm = assertLinearExecutionToEnd(writer.toString(), (CLASS_FIELD_NUMBER << 1) + 4);
    final int returnAddress = pop();
    final int objref = pop();

    assertTrue("Must be the address at the area", asm.findLabelAddress("STRT").intValue() < returnAddress && asm.findLabelAddress(END_LABEL).intValue() > returnAddress);
    assertEquals("Exception must be thrown", EXCEPTION_FLAG, peekb(EXCEPTION_FLAG_ADDRESS));
    assertStackEmpty();

  }


  @Override
  public String getAsmPrefix() {
    return "LD BC,EXSTART\n"
        + "LD (___ATHROW_PROCESSING_CODE_ADDRESS),BC\n"
        + "LD BC, " + CLASS_FIELD_NUMBER + "\n"
        + "LD DE," + LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3)) + '\n'
        + "CALL " + SUB_ALLOCATE_OBJECT + "\n"
        + "PUSH BC\n"
        + "JP STRT\n"
        + "EXSTART: LD A," + EXCEPTION_FLAG + "\n"
        + "LD (" + EXCEPTION_FLAG_ADDRESS + "),A\n"
        + "JP " + END_LABEL + "\n"
        + "STRT:\n";
  }

  @Override
  public String getAsmPostfix() {
    return CHECKCAST_TEXT + "\n" + ATHROWMANAGER_TEXT;
  }


  @Override
  public String prepareMemoryManagerText() {
    return INSTANCEOFMANAGER_TEXT.replace(MACRO_INSTANCEOFTABLE, postfixText) + "\n" + super.prepareMemoryManagerText();
  }
}
