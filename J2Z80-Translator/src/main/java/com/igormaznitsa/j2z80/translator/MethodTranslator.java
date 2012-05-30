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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
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
    private final ClassMethodInfo theMethod;

    public MethodTranslator(final TranslatorContext context, final ClassMethodInfo method) {
        this.translatorContext = context;
        this.theMethod = method;
    }

    public TranslatorContext getTranslatorContext() {
        return this.translatorContext;
    }

    public String[] translate() throws IOException {
        final List<String> asm = methodToAsm();
        final List<String> result = new ArrayList<String>();
        for(final String str : asm){
            final String [] splitted = Utils.breakToLines(str);
            for(final String s : splitted){
                result.add(s);
            }
        }
        
        return result.toArray(new String[result.size()]);
    }

    public ClassMethodInfo getMethod() {
        return theMethod;
    }

    private List<String> methodToAsm() throws IOException {
        final List<String> result = new ArrayList<String>(1024);

        result.add(LabelAndFrameUtils.makeLabelNameForMethod(theMethod) + ':');

        final MethodGen methodG = theMethod.getMethodGen();
        
        final InstructionList list = methodG.getInstructionList();
        list.setPositions();
        final InstructionHandle[] handles = list.getInstructionHandles();

        for (final InstructionHandle handler : handles) {
            final Instruction instruction = handler.getInstruction();

            final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());

            if (processor == null) {
                throw new IllegalArgumentException("Unsupported instruction detected [" + instruction.getName() + ']');
            }

            getTranslatorContext().registerAdditionsUsedByClass(processor.getClass());
            
            final StringWriter writer = new StringWriter(256);
            try {
                processor.process(this, instruction, handler, writer);
            }catch(IllegalArgumentException ex){
                getTranslatorContext().getLogger().logError(theMethod.toString()+" ["+ex.getMessage()+']');
                throw ex;
            }
            
            if (checkHandleForInstructionTargeters(handler)) {
                // the instruction is a jump target so we label it
                final String methodJumpLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(theMethod.getClassInfo(), theMethod.getMethodGen(), handler.getPosition());
                result.add(methodJumpLabel + ":\r\n");
            }

            result.add(writer.toString());
        }

        return result;
    }

    private boolean checkHandleForInstructionTargeters(final InstructionHandle handle){
        if (handle.hasTargeters()){
            for(final InstructionTargeter targeter : handle.getTargeters()){
                if (targeter instanceof Instruction){
                    return true;
                }
            }
        }
        return false;
    }
    
    public ConstantPoolGen getConstantPool() {
        return theMethod.getClassInfo().getConstantPool();
    }

    public String registerUsedConstantPoolItem(final int itemIndex) {
        final Constant item = getConstantPool().getConstant(itemIndex);

        String result;
        
        if (item instanceof ConstantString) {
            final ConstantUtf8 utfconst = (ConstantUtf8) getConstantPool().getConstant(( (ConstantString) item ).getStringIndex());
            result = LabelAndFrameUtils.makeLabelForConstantPoolItem(theMethod.getClassInfo(), ( (ConstantString) item ).getStringIndex());
            getTranslatorContext().registerConstantPoolItem(result, utfconst);
        } else {
            result = LabelAndFrameUtils.makeLabelForConstantPoolItem(theMethod.getClassInfo(), itemIndex);
            getTranslatorContext().registerConstantPoolItem(result, item);
        }
        return result;
    }

}
