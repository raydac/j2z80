package com.igormaznitsa.j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import org.apache.bcel.generic.Type;

public class System extends AbstractBootClass {

  private static final String[] OUT_STREAM = new String[] {"LD BC,#2", "PUSH BC"};
  private static final String[] ERR_STREAM = new String[] {"LD BC,#1", "PUSH BC"};

  @Override
  public String[] generateFieldGetter(final TranslatorContext translator, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    String[] data = null;
    if (isStatic) {
      if ("out".equals(fieldName)) {
        data = OUT_STREAM;
      } else if ("err".equals(fieldName)) {
        data = ERR_STREAM;
      } else {
        throwBootClassExceptionForField(fieldName, fieldType);
      }
    } else {
      throwBootClassExceptionForField(fieldName, fieldType);
    }
    return data;
  }

  @Override
  public String[] generateInvocation(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
    return null;
  }

  @Override
  public String[] generateFieldSetter(final TranslatorContext translator, final String fieldName,
                                      final Type methodSignature, final boolean isStatic) {
    throwBootClassExceptionForField(fieldName, methodSignature);
    return null;
  }

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }
}
