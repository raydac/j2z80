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
package com.igormaznitsa.j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import org.apache.bcel.generic.Type;

/**
 * The class implements stub for the java.lang.Object class from the standard Java framework.
 * At present it supports only the constructor and the hashCode method.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class Object extends AbstractBootClass {

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName, final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public String[] generateInvocation(final TranslatorContext translator, final String methodName, final Type[] methodArguments, final Type resultType) {
    if (methodArguments.length == 0) {
      if (resultType.getType() == Type.VOID.getType()) {
        if (methodName.equals("<init>")) {
          return new String[] {"POP BC ; call of Object.<init>, just drop the reference"};
        }
      } else if (resultType.getType() == Type.INT.getType()) {
        if (methodName.equals("hashCode")) {
          return new String[] {"; hashCode for Object just returns the object address which already on the stack"};
        }
      }
    }
    throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
    return null;
  }

  @Override
  public String[] generateFieldGetter(final TranslatorContext context, final String fieldName, final Type fieldType, final boolean isStatic) {
    throwBootClassExceptionForField(fieldName, fieldType);
    return null;
  }

  @Override
  public String[] generateFieldSetter(final TranslatorContext context, final String fieldName, final Type fieldType, final boolean isStatic) {
    throwBootClassExceptionForField(fieldName, fieldType);
    return null;
  }

}
