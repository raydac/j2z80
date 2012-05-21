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

import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import java.io.IOException;
import java.io.Writer;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.Type;

public abstract class AbstractFieldProcessor extends AbstractJvmCommandProcessor {

    protected boolean processBootClassCall(final MethodTranslator methodTranslator, final FieldInstruction instr, final Writer out) throws IOException {

        final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
        final ObjectType objType = (ObjectType) instr.getReferenceType(constantPool);
        final String className = objType.getClassName();
        final String fieldName = instr.getFieldName(constantPool);
        final Type fieldType = instr.getFieldType(constantPool);

        final Integer classGen = methodTranslator.getTranslatorContext().getClassContext().findClassUID(new ClassID(className));

        if (classGen != null) {
            return false;
        }

        final AbstractBootClass processor = AbstractBootClass.findProcessor(className);

        if (processor != null) {
            final boolean isStaticCall = ( instr instanceof PUTSTATIC ) || ( instr instanceof GETSTATIC );

            if (instr instanceof PUTSTATIC || instr instanceof PUTFIELD) {
                for (final String s : processor.generateSetField(methodTranslator.getTranslatorContext(), fieldName, fieldType, isStaticCall)) {
                    out.write(s);
                    if (!s.endsWith("\n")) {
                        out.write("\n");
                    }
                }
            } else if (instr instanceof GETSTATIC || instr instanceof GETFIELD) {
                for (final String s : processor.generateGetField(methodTranslator.getTranslatorContext(), fieldName, fieldType, isStaticCall)) {
                    out.write(s);
                    if (!s.endsWith("\n")) {
                        out.write("\n");
                    }
                }

            } else {
                throw new IllegalArgumentException("Unsupported field operation detected [" + instr + ']');
            }
            out.write("\n");

            methodTranslator.getTranslatorContext().registerCalledBootClassProcesser(processor);

            return true;
        }

        return false;
    }
}
