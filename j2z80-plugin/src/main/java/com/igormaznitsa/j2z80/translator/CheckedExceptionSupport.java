package com.igormaznitsa.j2z80.translator;

import static com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager.PENDING_EXCEPTION;
import static com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor.NEXT_LINE;
import static com.igormaznitsa.j2z80.translator.utils.ClassUtils.isJ2Z80ObjectClass;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_ATHROW;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * Checked-exception only model for Z80: a method that declares {@code throws} returns its value
 * as usual and also writes a hidden pending exception cell (0 = none). Call sites check that cell
 * and jump to a covering catch, or propagate with {@code RET}. Unchecked exceptions are rejected
 * at translate time.
 */
public enum CheckedExceptionSupport {
  ;

  public static boolean isUncheckedType(final String className, final ClassContext classContext) {
    Objects.requireNonNull(className, "className");
    String current = className;
    while (current != null) {
      if (isUncheckedRoot(current)) {
        return true;
      }
      if (isCheckedRoot(current) || isJ2Z80ObjectClass(current) ||
          "java.lang.Object".equals(current)) {
        return false;
      }
      final ClassGen classGen = classContext.findClassForID(new ClassID(current));
      if (classGen == null) {
        return false;
      }
      current = classGen.getSuperclassName();
    }
    return false;
  }

  public static boolean declaresCheckedExceptions(final MethodGen method) {
    return Stream.ofNullable(method.getExceptions())
        .flatMap(Arrays::stream)
        .findAny()
        .isPresent();
  }

  public static void rejectUncheckedType(
      final TranslatorContext translator,
      final String className,
      final String usage
  ) {
    if (isUncheckedType(className, translator.getClassContext())) {
      final String message = "Unchecked exception is not supported (" + usage + "): " + className;
      translator.getLogger().logError(message);
      throw new IllegalArgumentException(message);
    }
  }

  public static void validateMethodExceptions(final TranslatorContext translator,
                                              final MethodGen method) {
    Stream.ofNullable(method.getExceptions())
        .flatMap(Arrays::stream)
        .forEach(type -> rejectUncheckedType(translator, type, "throws of " + method.getName()));
    final CodeExceptionGen[] handlers = method.getExceptionHandlers();
    if (handlers == null) {
      return;
    }
    Arrays.stream(handlers)
        .map(CodeExceptionGen::getCatchType)
        .filter(Objects::nonNull)
        .map(ObjectType::getClassName)
        .forEach(type -> rejectUncheckedType(translator, type, "catch in " + method.getName()));
  }

  public static void validateClasspath(final TranslatorContext translator) {
    final ClassContext classContext = translator.getClassContext();
    for (final ClassID classId : classContext.getAllClasses()) {
      final ClassGen classGen = classContext.findClassForID(classId);
      if (classGen == null) {
        continue;
      }
      final String superName = classGen.getSuperclassName();
      if (isUncheckedRoot(classGen.getClassName())
          || isUncheckedType(superName, classContext)) {
        rejectUncheckedType(translator, classGen.getClassName(),
            "class " + classGen.getClassName() + " extends " + superName);
      }
    }
  }

  public static String clearPendingException() {
    return "LD HL,0" + NEXT_LINE
        + "LD (" + PENDING_EXCEPTION + "),HL" + NEXT_LINE;
  }

  public static String exceptionalReturn() {
    return "LD (" + PENDING_EXCEPTION + "),BC" + NEXT_LINE
        + "RET" + NEXT_LINE;
  }

  public static String discardReturnedValue(final Type returnType) {
    if (returnType.getType() == Type.VOID.getType()) {
      return "";
    }
    if (returnType.getSize() == 2) {
      return "POP BC" + NEXT_LINE + "POP DE" + NEXT_LINE;
    }
    return "POP BC" + NEXT_LINE;
  }

  public static String afterInvokeCheck(
      final MethodTranslator methodTranslator,
      final InstructionHandle invokeHandle,
      final MethodGen invokedMethod
  ) {
    if (!declaresCheckedExceptions(invokedMethod)) {
      return "";
    }

    methodTranslator.translatorContext().registerAdditionsUsedByClass(Processor_ATHROW.class);

    final ClassMethodInfo caller = methodTranslator.method();
    final int position = invokeHandle.getPosition();
    final String okLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(caller, position) + "_EXOK";

    return "LD HL,(" + PENDING_EXCEPTION + ")" + NEXT_LINE
        + "LD A,H" + NEXT_LINE
        + "OR L" + NEXT_LINE
        + "JP Z," + okLabel + NEXT_LINE
        + discardReturnedValue(invokedMethod.getReturnType())
        + "LD BC,(" + PENDING_EXCEPTION + ")" + NEXT_LINE
        + dispatchExceptionInBc(methodTranslator, invokeHandle)
        + okLabel + ':' + NEXT_LINE;
  }

  public static String translateAthrow(
      final MethodTranslator methodTranslator,
      final InstructionHandle athrowHandle
  ) {
    methodTranslator.translatorContext().registerAdditionsUsedByClass(Processor_ATHROW.class);

    return "POP BC" + NEXT_LINE
        + dispatchExceptionInBc(methodTranslator, athrowHandle);
  }

  public static String clearPendingOnReturn(final MethodGen method) {
    return declaresCheckedExceptions(method) ? clearPendingException() : "";
  }

  private static String dispatchExceptionInBc(
      final MethodTranslator methodTranslator,
      final InstructionHandle throwSite
  ) {
    final MethodGen method = methodTranslator.method().getMethodGen();
    final StringBuilder assembly = new StringBuilder();
    final CodeExceptionGen[] handlers = method.getExceptionHandlers();

    Stream.of(handlers == null ? new CodeExceptionGen[0] : handlers)
        .filter(handler -> covers(handler, throwSite))
        .filter(handler -> handler.getHandlerPC() != throwSite)
        .forEach(handler -> assembly.append(matchHandler(methodTranslator, handler, throwSite)));

    if (declaresCheckedExceptions(method)) {
      assembly.append(exceptionalReturn());
    } else {
      assembly.append("JP 0").append(NEXT_LINE);
    }
    return assembly.toString();
  }

  private static String matchHandler(
      final MethodTranslator methodTranslator,
      final CodeExceptionGen handler,
      final InstructionHandle throwSite
  ) {
    final String handlerLabel = LabelAndFrameUtils.makeClassMethodJumpLabel(
        methodTranslator.method(), handler.getHandlerPC().getPosition());
    final ObjectType catchType = handler.getCatchType();

    if (catchType == null) {
      return clearPendingException()
          + "PUSH BC" + NEXT_LINE
          + "JP " + handlerLabel + NEXT_LINE;
    }

    rejectUncheckedType(methodTranslator.translatorContext(), catchType.getClassName(),
        "catch handler");

    final ClassID catchClass = new ClassID(catchType.getClassName());
    methodTranslator.translatorContext().registerClassForCastCheck(catchClass);
    final String typeLabel = LabelAndFrameUtils.makeLabelForClassID(catchClass);
    final String nextLabel = handlerLabel + "_EXN"
        + throwSite.getPosition() + '_'
        + handler.getStartPC().getPosition() + '_'
        + handler.getHandlerPC().getPosition();

    return "PUSH BC" + NEXT_LINE
        + "LD DE," + typeLabel + NEXT_LINE
        + "CALL ___INSTANCE_OF" + NEXT_LINE
        + "LD A,B" + NEXT_LINE
        + "OR C" + NEXT_LINE
        + "POP BC" + NEXT_LINE
        + "JP Z," + nextLabel + NEXT_LINE
        + clearPendingException()
        + "PUSH BC" + NEXT_LINE
        + "JP " + handlerLabel + NEXT_LINE
        + nextLabel + ':' + NEXT_LINE;
  }

  private static boolean covers(final CodeExceptionGen handler, final InstructionHandle site) {
    final InstructionHandle start = handler.getStartPC();
    final InstructionHandle end = handler.getEndPC();
    if (start == null || end == null || site == null) {
      return false;
    }
    final int position = site.getPosition();
    final InstructionHandle exclusiveEnd = end.getNext();
    if (exclusiveEnd == null) {
      return position >= start.getPosition();
    }
    return position >= start.getPosition() && position < exclusiveEnd.getPosition();
  }

  private static boolean isUncheckedRoot(final String className) {
    return "java.lang.RuntimeException".equals(className)
        || "java.lang.Error".equals(className)
        || "j2z80.bootstrap.java.lang.RuntimeException".equals(className)
        || "j2z80.bootstrap.java.lang.Error".equals(className);
  }

  private static boolean isCheckedRoot(final String className) {
    return "java.lang.Exception".equals(className)
        || "java.lang.Throwable".equals(className)
        || "j2z80.bootstrap.java.lang.Exception".equals(className)
        || "j2z80.bootstrap.java.lang.Throwable".equals(className);
  }
}
