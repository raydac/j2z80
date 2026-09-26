package j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import org.apache.bcel.generic.Type;

public class Exception extends AbstractBootstrapClass {

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public String[] generateInvocation(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    if ("<init>".equals(methodName) && methodArguments.length == 0
        && resultType.getType() == Type.VOID.getType()) {
      return new String[] {"POP BC"};
    }
    this.throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
    return null;
  }

  @Override
  public String[] generateFieldGetter(final TranslatorContext context, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    this.throwBootClassExceptionForField(fieldName, fieldType);
    return null;
  }

  @Override
  public String[] generateFieldSetter(final TranslatorContext context, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    this.throwBootClassExceptionForField(fieldName, fieldType);
    return null;
  }
}
