package j2z80.bootstrap.java.lang;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import java.util.List;
import org.apache.bcel.generic.Type;

/**
 * Bootstrap emulator for {@link java.lang.RuntimeException}. It models the default runtime
 * exception constructor required by the translator without attempting to implement the entire
 * Java exception hierarchy.
 */
public class RuntimeException extends AbstractBootstrapClass {

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public List<String> generateInvocation(final TranslatorContext translator,
                                         final String methodName,
                                         final Type[] methodArguments, final Type resultType) {
    if ("<init>".equals(methodName) && methodArguments.length == 0
        && resultType.getType() == Type.VOID.getType()) {
      return List.of("POP BC");
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
