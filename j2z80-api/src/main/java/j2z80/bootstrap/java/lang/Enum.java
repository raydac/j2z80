/*
 * Copyright 2012-2026 Igor Maznitsa.
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

package j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.bootstrap.BootClassException;
import java.util.List;
import org.apache.bcel.generic.Type;

/**
 * Bootstrap emulator for {@link java.lang.Enum}. The translator uses this class to generate code
 * for enum constructors, the {@code ordinal()} accessor, and the subset of enum operations that
 * can be represented without a full Java String implementation.
 */
public class Enum extends AbstractBootstrapClass {

  /**
   * Loads the enum ordinal from the object memory layout used by the translator.
   *
   * @return the assembly instructions that pop the enum reference, read the stored ordinal, and
   * push the resulting integer back onto the stack
   */
  public static List<String> ordinalRead() {
    return List.of(
        "POP HL",
        "LD C,(HL)",
        "INC HL",
        "LD B,(HL)",
        "PUSH BC"
    );
  }

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public List<String> generateInvocation(final TranslatorContext translator,
                                         final String methodName,
                                         final Type[] methodArguments, final Type resultType) {
    if ("<init>".equals(methodName)
        && methodArguments.length == 2
        && methodArguments[1].getType() == Type.INT.getType()
        && resultType.getType() == Type.VOID.getType()) {
      return List.of(
          "POP HL",
          "POP BC",
          "POP DE",
          "LD A,L",
          "LD (DE),A",
          "INC DE",
          "LD A,H",
          "LD (DE),A"
      );
    }
    if ("ordinal".equals(methodName)
        && methodArguments.length == 0
        && resultType.getType() == Type.INT.getType()) {
      return ordinalRead();
    }
    if ("name".equals(methodName) || "valueOf".equals(methodName)) {
      throw new BootClassException(
          "Enum." + methodName + " is not supported because String is not supported",
          "java.lang.Enum", methodName, Type.getMethodSignature(resultType, methodArguments));
    }
    this.throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
    return List.of();
  }

  @Override
  public List<String> generateFieldGetter(final TranslatorContext context, final String fieldName,
                                          final Type fieldType, final boolean isStatic) {
    this.throwBootClassExceptionForField(fieldName, fieldType);
    return List.of();
  }

  @Override
  public List<String> generateFieldSetter(final TranslatorContext context, final String fieldName,
                                          final Type fieldType, final boolean isStatic) {
    this.throwBootClassExceptionForField(fieldName, fieldType);
    return List.of();
  }
}
