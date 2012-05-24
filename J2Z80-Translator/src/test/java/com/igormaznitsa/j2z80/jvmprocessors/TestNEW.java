/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 * 
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.ids.ClassID;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class TestNEW extends AbstractTestBasedOnMemoryManager {
    public static final int CLASS_INDEX = CONSTANT_USER_DEFINED+2;
    public static final int CLASS_NAME_INDEX = CONSTANT_USER_DEFINED+1;
    
    public static final int TEST_OBJECT_SIZE = 34;
    public final String TEST_CLASS_NAME = "some.test.class";
    
    @Test(timeout=3000L)
    public void testObjectCreation() throws Exception {

        final String testClassName = "some.test.class";
        
        when(CP_GEN_MOCK.getConstant(CLASS_NAME_INDEX)).thenReturn(new ConstantUtf8(testClassName));
        when(CP_GEN_MOCK.getConstant(CLASS_INDEX)).thenReturn(new ConstantClass(CLASS_NAME_INDEX));
        when(CP_MOCK.getConstantString(CLASS_INDEX, Constants.CONSTANT_Class)).thenReturn(testClassName);
        
        final ClassID cid = new ClassID(testClassName);
        
        when(CLASSCONTEXT_MOCK.findClassUID(eq(cid))).thenReturn(Integer.valueOf(0xCAFE));
        
        assertAllocateCommand(new Instruction[]{new NEW(CLASS_INDEX)},(TEST_OBJECT_SIZE<<1)+4);
        assertEquals("Check the initial address for the new object",getInitialMemoryAddress()+4,pop());
        assertEquals("Check the size in cells",TEST_OBJECT_SIZE, peekw(getInitialMemoryAddress()));
        assertEquals("Check class ID",0xCAFE, peekw(getInitialMemoryAddress()+2));
        assertStackEmpty();
    }

    @Override
    public String getAsmPostfix() {
        final String label = LabelAndFrameUtils.makeLabelForClassSizeInfo(new ObjectType(TEST_CLASS_NAME));
        return label+": EQU "+TEST_OBJECT_SIZE;
    }
}
