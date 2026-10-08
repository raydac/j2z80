package j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import java.util.List;
import org.apache.bcel.generic.Type;

public class System extends Object {

  private static final List<String> OUT_STREAM = List.of("LD BC,#2", "PUSH BC");
  private static final List<String> ERR_STREAM = List.of("LD BC,#1", "PUSH BC");

  @Override
  public List<String> generateFieldGetter(final TranslatorContext translator,
                                          final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    final List<String> data;
    if (isStatic) {
      if ("out".equals(fieldName)) {
        data = OUT_STREAM;
      } else if ("err".equals(fieldName)) {
        data = ERR_STREAM;
      } else {
        this.throwBootClassExceptionForField(fieldName, fieldType);
        data = List.of();
      }
    } else {
      this.throwBootClassExceptionForField(fieldName, fieldType);
      data = List.of();
    }
    return data;
  }

  @Override
  public List<String> generateInvocation(final TranslatorContext translator,
                                         final String methodName,
                                         final Type[] methodArguments, final Type resultType) {
    this.throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
    return List.of();
  }

  @Override
  public List<String> generateFieldSetter(final TranslatorContext translator,
                                          final String fieldName,
                                      final Type methodSignature, final boolean isStatic) {
    this.throwBootClassExceptionForField(fieldName, methodSignature);
    return List.of();
  }

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }
}
