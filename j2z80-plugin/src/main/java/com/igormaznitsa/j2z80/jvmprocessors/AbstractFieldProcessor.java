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

import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
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
   * @param bootstrapClassLoader bootstrap class loader, must not be null
   * @param out              a writer to make output for assembler instructions, must not be null
   * @return true if the field instruction processes a bootstrap class field, else false
   * @throws IOException it will be thrown if there is any transport level error
   */
  protected boolean processBootstrapClassCall(
      final MethodTranslator methodTranslator,
      final FieldInstruction instruction,
      final ClassLoader bootstrapClassLoader,
      final Writer out
  ) throws IOException {

    final ConstantPoolGen constantPool = methodTranslator.getConstantPool();
    final ObjectType objType = (ObjectType) instruction.getReferenceType(constantPool);
    final String className = objType.getClassName();
    final String fieldName = instruction.getFieldName(constantPool);
    final Type fieldType = instruction.getFieldType(constantPool);

    final Integer classGen = methodTranslator.getTranslatorContext().getClassContext().findClassUID(new ClassID(className));

    if (classGen != null) {
      return false;
    }

    final AbstractBootstrapClass processor =
        AbstractBootstrapClass.findProcessor(className, bootstrapClassLoader);

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
