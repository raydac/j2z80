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
import java.util.List;
import org.apache.bcel.generic.Type;

/**
 * Bootstrap emulator for {@link java.lang.Record}. It provides the minimal constructor behavior
 * needed by the translator to handle record classes and their generated accessors without a full
 * Java runtime implementation.
 */
public class Record extends AbstractBootstrapClass {

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public List<String> generateInvocation(final TranslatorContext translator,
                                         final String methodName,
                                         final Type[] methodArguments, final Type resultType) {
    if (methodArguments.length == 0
        && resultType.getType() == Type.VOID.getType()
        && "<init>".equals(methodName)) {
      return List.of("POP BC ; call of Record.<init>, just drop the reference");
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
