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

package com.igormaznitsa.j2z80.translator;

import static com.igormaznitsa.j2z80.translator.utils.ClassUtils.isJ2Z80ObjectClass;

import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.ids.ClassID;
import j2z80.bootstrap.java.lang.Enum;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ARETURN;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public final class EnumSupport {

  private static final String ENUM_CLASS = "java.lang.Enum";
  private static final String NO_SUCH_FIELD = "java.lang.NoSuchFieldError";

  private final Map<String, ClassGen> classes;
  private final TranslatorLogger logger;

  private EnumSupport(final Map<String, ClassGen> classes, final TranslatorLogger logger) {
    this.classes = classes;
    this.logger = logger;
  }

  public static void rewrite(final Map<String, ClassGen> classes, final TranslatorLogger logger) {
    new EnumSupport(classes, logger).rewriteEnums();
  }

  public static boolean writeOrdinal(
      final MethodTranslator methodTranslator,
      final InvokeInstruction instruction,
      final Writer out
  ) throws IOException {
    if (!isOrdinal(methodTranslator, instruction)
        || !isEnumHierarchy(methodTranslator, classNameOf(methodTranslator, instruction))) {
      return false;
    }
    for (final String line : Enum.ordinalRead()) {
      out.write(line);
      out.write('\n');
    }
    return true;
  }

  public static void rejectStringOnlyEnumUse(
      final MethodTranslator methodTranslator,
      final InvokeInstruction instruction
  ) {
    final String name = instruction.getMethodName(methodTranslator.getConstantPool());
    final String signature = instruction.getSignature(methodTranslator.getConstantPool());
    if (!"valueOf".equals(name) &&
        !("name".equals(name) && "()Ljava/lang/String;".equals(signature))) {
      return;
    }
    final String className = classNameOf(methodTranslator, instruction);
    if (!ENUM_CLASS.equals(className) && !isEnumHierarchy(methodTranslator, className)) {
      return;
    }
    final String message = "Enum." + name + " is not supported because String is not supported";
    methodTranslator.translatorContext().getLogger().logError(message);
    throw new IllegalArgumentException(message);
  }

  private static boolean isOrdinal(final MethodTranslator methodTranslator,
                                   final InvokeInstruction instruction) {
    return "ordinal".equals(instruction.getMethodName(methodTranslator.getConstantPool()))
        && "()I".equals(instruction.getSignature(methodTranslator.getConstantPool()));
  }

  private static String classNameOf(final MethodTranslator methodTranslator,
                                    final InvokeInstruction instruction) {
    final Type type = instruction.getReferenceType(methodTranslator.getConstantPool());
    if (!(type instanceof ObjectType)) {
      return "";
    }
    return type.getClassName();
  }

  private static boolean isEnumHierarchy(final MethodTranslator methodTranslator,
                                         final String className) {
    if (ENUM_CLASS.equals(className)) {
      return true;
    }
    ClassGen current = methodTranslator.translatorContext().getClassContext()
        .findClassForID(new ClassID(className));
    while (current != null) {
      if (current.isEnum() && ENUM_CLASS.equals(current.getSuperclassName())) {
        return true;
      }
      final String superName = current.getSuperclassName();
      if (superName == null || isJ2Z80ObjectClass(superName)) {
        return false;
      }
      current = methodTranslator.translatorContext().getClassContext()
          .findClassForID(new ClassID(superName));
    }
    return false;
  }

  private void rewriteEnums() {
    for (final ClassGen classGen : this.classes.values()) {
      if (classGen.isEnum()) {
        this.rewriteEnum(classGen);
      }
      if (this.isSwitchMap(classGen)) {
        this.dropNoSuchFieldHandlers(classGen);
      }
    }
  }

  private void rewriteEnum(final ClassGen classGen) {
    final Method[] methods = classGen.getMethods();
    boolean rewritten = false;
    for (int index = 0; index < methods.length; index++) {
      final Method method = methods[index];
      if (this.isValues(method)) {
        classGen.setMethodAt(this.methodWith(classGen, method, this.valuesBody(classGen), 4, 3),
            index);
        rewritten = true;
      } else if (this.isValueOf(method)) {
        classGen.setMethodAt(this.methodWith(classGen, method, this.nullBody(), 1, 1), index);
        rewritten = true;
      }
    }
    if (rewritten) {
      this.logger.logInfo("Rewrote enum values() of " + classGen.getClassName());
    }
  }

  private InstructionList valuesBody(final ClassGen classGen) {
    final Field valuesField = this.valuesField(classGen);
    final InstructionFactory factory = new InstructionFactory(classGen);
    final InstructionList code = new InstructionList();
    final Type arrayType = valuesField.getType();

    code.append(factory.createGetStatic(classGen.getClassName(), valuesField.getName(), arrayType));
    code.append(new ASTORE(0));
    code.append(new ALOAD(0));
    code.append(new ARRAYLENGTH());
    code.append(factory.createNewArray(((ArrayType) arrayType).getElementType(), (short) 1));
    code.append(new ASTORE(1));
    code.append(new ICONST(0));
    code.append(new ISTORE(2));

    final GOTO toCheck = new GOTO(null);
    code.append(toCheck);

    final InstructionHandle body = code.append(new ALOAD(1));
    code.append(new ILOAD(2));
    code.append(new ALOAD(0));
    code.append(new ILOAD(2));
    code.append(new AALOAD());
    code.append(new AASTORE());
    code.append(new IINC(2, 1));

    final InstructionHandle check = code.append(new ILOAD(2));
    code.append(new ALOAD(0));
    code.append(new ARRAYLENGTH());
    code.append(new IF_ICMPLT(body));
    toCheck.setTarget(check);

    code.append(new ALOAD(1));
    code.append(new ARETURN());
    return code;
  }

  private InstructionList nullBody() {
    final InstructionList code = new InstructionList();
    code.append(new ACONST_NULL());
    code.append(new ARETURN());
    return code;
  }

  private Field valuesField(final ClassGen classGen) {
    for (final Field field : classGen.getFields()) {
      if (field.isStatic() && "$VALUES".equals(field.getName()) &&
          field.getType() instanceof ArrayType) {
        return field;
      }
    }
    throw new IllegalArgumentException("Enum " + classGen.getClassName() + " has no $VALUES array");
  }

  private void dropNoSuchFieldHandlers(final ClassGen classGen) {
    final Method[] methods = classGen.getMethods();
    for (int index = 0; index < methods.length; index++) {
      final MethodGen methodGen = new MethodGen(
          methods[index], classGen.getClassName(), classGen.getConstantPool());
      final List<CodeExceptionGen> dropped = new ArrayList<>();
      for (final CodeExceptionGen handler : methodGen.getExceptionHandlers()) {
        if (handler.getCatchType() != null
            && NO_SUCH_FIELD.equals(handler.getCatchType().getClassName())) {
          dropped.add(handler);
        }
      }
      if (dropped.isEmpty()) {
        continue;
      }
      dropped.forEach(methodGen::removeExceptionHandler);
      classGen.setMethodAt(methodGen.getMethod(), index);
    }
  }

  private boolean isSwitchMap(final ClassGen classGen) {
    return Arrays.stream(classGen.getFields())
        .anyMatch(field -> field.isStatic() && field.getName().startsWith("$SwitchMap"));
  }

  private boolean isValues(final Method method) {
    return method.isStatic()
        && "values".equals(method.getName())
        && method.getArgumentTypes().length == 0
        && method.getReturnType() instanceof ArrayType;
  }

  private boolean isValueOf(final Method method) {
    return method.isStatic() && "valueOf".equals(method.getName());
  }

  private Method methodWith(
      final ClassGen classGen,
      final Method method,
      final InstructionList body,
      final int stack,
      final int locals
  ) {
    try {
      final MethodGen methodGen =
          new MethodGen(method, classGen.getClassName(), classGen.getConstantPool());
      methodGen.removeCodeAttributes();
      methodGen.removeLineNumbers();
      methodGen.removeLocalVariables();
      methodGen.removeLocalVariableTypeTable();
      methodGen.removeExceptionHandlers();
      methodGen.setInstructionList(body);
      methodGen.setMaxStack(stack);
      methodGen.setMaxLocals(locals);
      return methodGen.getMethod();
    } finally {
      body.dispose();
    }
  }
}
