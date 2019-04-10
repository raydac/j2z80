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
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.Type;

import java.io.IOException;
import java.io.Writer;

/**
 * The class is ancestor for all field processors
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractFieldProcessor extends AbstractJvmCommandProcessor {

  /**
   * Check the invoke instruction for a bootstrap class and if the field is situated in
   * a bootstrap class  then the method will process it by a special way.
   *
   * @param methodTranslator a method translator, must not be null
   * @param instruction      a field instruction, must not be null
   * @param out              a writer to make output for assembler instructions, must not be null
   * @return true if the field instruction processes a bootstrap class field, else false
   * @throws IOException it will be thrown if there is any transport level error
   */
  protected boolean processBootClassCall(final MethodTranslator methodTranslator, final FieldInstruction instruction, final Writer out) throws IOException {

    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    final ObjectType objType = (ObjectType) instruction.getReferenceType(constantPool);
    final String className = objType.getClassName();
    final String fieldName = instruction.getFieldName(constantPool);
    final Type fieldType = instruction.getFieldType(constantPool);

    final Integer classGen = methodTranslator.getTranslatorContext().getClassContext().findClassUID(new ClassID(className));

    if (classGen != null) {
      return false;
    }

    final AbstractBootClass processor = AbstractBootClass.findProcessor(className);

    if (processor != null) {
      final boolean isStaticCall = (instruction instanceof PUTSTATIC) || (instruction instanceof GETSTATIC);

      if (instruction instanceof PUTSTATIC || instruction instanceof PUTFIELD) {
        for (final String s : processor.generateFieldSetter(methodTranslator.getTranslatorContext(), fieldName, fieldType, isStaticCall)) {
          out.write(s);
          if (!s.endsWith("\n")) {
            out.write(NEXT_LINE);
          }
        }
      } else if (instruction instanceof GETSTATIC || instruction instanceof GETFIELD) {
        for (final String s : processor.generateFieldGetter(methodTranslator.getTranslatorContext(), fieldName, fieldType, isStaticCall)) {
          out.write(s);
          if (!s.endsWith("\n")) {
            out.write(NEXT_LINE);
          }
        }

      } else {
        throw new IllegalArgumentException("Unsupported field operation detected [" + instruction + ']');
      }
      out.write(NEXT_LINE);

      methodTranslator.getTranslatorContext().registerCalledBootClassProcesser(processor);

      return true;
    }

    return false;
  }
}
