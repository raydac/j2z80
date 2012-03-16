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

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.LabelUtils;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.*;
import org.apache.bcel.generic.*;

// class to process NEW with code 187
public class Processor_NEW extends AbstractJvmCommandProcessor implements NeedsMemoryManager {
    private final String template; 
    
    public Processor_NEW(){
        super();
        template = loadResourceFileAsString("NEW.a80").replace(MACROS_ADDRESS, SUB_ALLOCATE_OBJECT);
    }

    @Override
    public String getName() {
        return "NEW";
    }

    @Override
    public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
        final NEW newins = (NEW)instruction;
        final ObjectType type = newins.getLoadClassType(methodTranslator.getConstantPool());
        final String className =  type.getClassName();
        
        final Integer classID = methodTranslator.getTranslatorContext().getClassContext().findClassUID(new ClassID(className));
        
        Assert.assertNotNull("Class ID must not be null [" + className + ']', classID);
        
        final String classInfoLabel = LabelUtils.makeLabelForClassSizeInfo(type);
        
        out.write(template.replace(MACROS_VALUE, classInfoLabel).replace(MACROS_ID, Integer.toString(classID)));
        out.write(NEXT_LINE);
    }
}
