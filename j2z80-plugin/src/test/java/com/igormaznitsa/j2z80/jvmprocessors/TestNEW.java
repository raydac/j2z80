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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ObjectType;
import org.junit.Test;

public class TestNEW extends AbstractTestBasedOnMemoryManager {
  public static final int CLASS_INDEX = CONSTANT_USER_DEFINED + 2;
  public static final int CLASS_NAME_INDEX = CONSTANT_USER_DEFINED + 1;

  public static final int TEST_OBJECT_SIZE = 34;
  public final String TEST_CLASS_NAME = "some.test.class";

  @Test(timeout = 3000L)
  public void testObjectCreation() throws Exception {

    final String testClassName = "some.test.class";

    when(CP_GEN_MOCK.getConstant(CLASS_NAME_INDEX)).thenReturn(new ConstantUtf8(testClassName));
    when(CP_GEN_MOCK.getConstant(CLASS_INDEX)).thenReturn(new ConstantClass(CLASS_NAME_INDEX));
    when(CP_MOCK.getConstantString(CLASS_INDEX, Const.CONSTANT_Class)).thenReturn(testClassName);

    final ClassID cid = new ClassID(testClassName);

    when(CLASSCONTEXT_MOCK.findClassUID(eq(cid))).thenReturn(Integer.valueOf(0xCAFE));

    assertAllocateCommand(new Instruction[] {new NEW(CLASS_INDEX)}, (TEST_OBJECT_SIZE << 1) + 4);
    assertEquals("Check the initial address for the new object", getInitialMemoryAddress() + 4, pop());
    assertEquals("Check the size in cells", TEST_OBJECT_SIZE, peekw(getInitialMemoryAddress()));
    assertEquals("Check class ID", 0xCAFE, peekw(getInitialMemoryAddress() + 2));
    assertStackEmpty();
  }

  @Override
  public String getAsmPostfix() {
    final String label = LabelAndFrameUtils.makeLabelForClassSizeInfo(new ObjectType(TEST_CLASS_NAME));
    return label + ": EQU " + TEST_OBJECT_SIZE;
  }
}
