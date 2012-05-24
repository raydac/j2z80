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

import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.aux.InstanceofTable;
import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.*;
import java.util.HashSet;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import static org.junit.Assert.*;
import org.junit.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TestCHECKCAST extends AbstractTestBasedOnMemoryManager implements NeedsInstanceofManager {
    
    protected static final String CHECKCAST_TEXT;
    protected static final String ATHROWMANAGER_TEXT;

    private static final int EXCEPTION_FLAG_ADDRESS = 0x100;
    private static final int EXCEPTION_FLAG = 0xFF;
    
    static {
        try {
            CHECKCAST_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "CHECKCAST_MANAGER.a80");
        }
        catch (IOException ex) {
            throw new Error("Can't read checkcast manager assembler text");
        }
        try {
            ATHROWMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "ATHROW_MANAGER.a80");
        }
        catch (IOException ex) {
            throw new Error("Can't read athrow manager assembler text");
        }
    }
    
    private static final String TEST_CLASS_NAME1 = "test.instanceof.class1";
    private static final Integer TEST_CLASS_NAME1_ID = Integer.valueOf(0x101);
    
    private static final String TEST_CLASS_NAME2 = "test.instanceof.class2";
    private static final Integer TEST_CLASS_NAME2_ID = Integer.valueOf(0x102);

    private static final String TEST_CLASS_NAME3 = "test.instanceof.class3";
    private static final Integer TEST_CLASS_NAME3_ID = Integer.valueOf(0x103);

    private String postfixText;
    
    private static final int CLASS_NAME_INDEX = CONSTANT_USER_DEFINED+1;
    private static final int CLASS_INDEX = CONSTANT_USER_DEFINED+2;
    
    private static final int CLASS_FIELD_NUMBER = 32;
    
    @Before
    public void prepareTest(){
        final ConstantUtf8 cpClassName = new ConstantUtf8(TEST_CLASS_NAME1);
        final ConstantClass cpClass = new ConstantClass(CLASS_NAME_INDEX);
        when(CP_GEN_MOCK.getConstant(CLASS_NAME_INDEX)).thenReturn(cpClassName);
        when(CP_GEN_MOCK.getConstant(CLASS_INDEX)).thenReturn(cpClass);
        when(CP_MOCK.getConstantString(CLASS_INDEX, Constants.CONSTANT_Class)).thenReturn(TEST_CLASS_NAME1);
        
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
    
    private void prepareTabe(final boolean successor){
        final StringBuilder bldr = new StringBuilder();
        
        final InstanceofTable table = new InstanceofTable(TRANSLATOR_MOCK,new HashSet<ClassID>());
        if (successor){
            final InstanceofTable.InstanceofRow r = table.addRow(new ClassID(TEST_CLASS_NAME1));
            r.addClass(new ClassID(TEST_CLASS_NAME2));
            r.addClass(new ClassID(TEST_CLASS_NAME3));
        } else {
            final InstanceofTable.InstanceofRow r = table.addRow(new ClassID(TEST_CLASS_NAME1));
            r.addClass(new ClassID(TEST_CLASS_NAME2));
        }
        
        // add class ids
        bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME1))).append(": EQU ").append(TEST_CLASS_NAME1_ID.intValue()).append('\n');
        bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME2))).append(": EQU ").append(TEST_CLASS_NAME2_ID.intValue()).append('\n');
        bldr.append(LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3))).append(": EQU ").append(TEST_CLASS_NAME3_ID.intValue()).append('\n');
        
        postfixText = table.toAsm()+"\n";
        
        postfixText += bldr.toString();
    }
    
    @Test(timeout=3000L)
    public void testClassIsSuccessor() throws IOException {
        prepareTabe(true);
        
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(CHECKCAST.class);
        final StringWriter writer = new StringWriter();

        processor.process(CLASS_PROCESSOR_MOCK, new CHECKCAST(CLASS_INDEX), mock(InstructionHandle.class), writer);
        assertLinearExecutionToEnd(writer.toString(),(CLASS_FIELD_NUMBER<<1)+4);
        final int stacktop = pop();
        assertStackEmpty();
        assertEquals("The class must be instanceof for the #2211",getInitialMemoryAddress()+4,stacktop);
    }

    @Test(timeout=3000L)
    public void testClassIsNotSuccessor() throws IOException {
        prepareTabe(false);
        
        final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(CHECKCAST.class);
        final StringWriter writer = new StringWriter();

        processor.process(CLASS_PROCESSOR_MOCK, new CHECKCAST(CLASS_INDEX), mock(InstructionHandle.class), writer);
        final Z80Asm asm = assertLinearExecutionToEnd(writer.toString(),(CLASS_FIELD_NUMBER<<1)+4);
        final int returnAddress = pop();
        final int objref = pop();

        assertTrue("Must be the address at the area",asm.findLabelAddress("STRT").intValue()<returnAddress && asm.findLabelAddress(END_LABEL).intValue()>returnAddress);
        assertEquals("Exception must be thrown",EXCEPTION_FLAG,peekb(EXCEPTION_FLAG_ADDRESS));
        assertStackEmpty();
        
    }


    @Override
    public String getAsmPrefix() {
        return  "LD BC,EXSTART\n"
                +"LD (___ATHROW_PROCESSING_CODE_ADDRESS),BC\n"
                +"LD BC, "+CLASS_FIELD_NUMBER+"\n"
                +"LD DE,"+LabelAndFrameUtils.makeLabelForClassID(new ClassID(TEST_CLASS_NAME3))+'\n'
                +"CALL "+ SUB_ALLOCATE_OBJECT+"\n"
                +"PUSH BC\n"
                +"JP STRT\n"
                +"EXSTART: LD A,"+EXCEPTION_FLAG+"\n"
                +"LD ("+EXCEPTION_FLAG_ADDRESS+"),A\n"
                +"JP "+END_LABEL+"\n"
                +"STRT:\n";
    }

    @Override
    public String getAsmPostfix() {
        return CHECKCAST_TEXT+"\n"+ATHROWMANAGER_TEXT;
    }

    
    
    @Override
    public String prepareMemoryManagerText() {
        return INSTANCEOFMANAGER_TEXT.replace(MACRO_INSTANCEOFTABLE, postfixText)+"\n"+super.prepareMemoryManagerText();
    }
}
