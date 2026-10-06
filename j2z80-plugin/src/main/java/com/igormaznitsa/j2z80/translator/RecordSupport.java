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

import static java.util.stream.Collectors.toList;
import static org.apache.bcel.Const.INVOKEVIRTUAL;

import com.igormaznitsa.j2z80.TranslatorLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.bcel.generic.IF_ACMPNE;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKEDYNAMIC;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * javac implements record {@code equals}, {@code hashCode}, and {@code toString} as
 * {@code invokedynamic} bootstrapped by {@code java.lang.runtime.ObjectMethods}. Those bodies
 * are replaced with bytecode this translator can emit. {@code toString} returns null.
 */
public final class RecordSupport {

  private static final String RECORD_CLASS = "java.lang.Record";
  private static final int HASH_MULTIPLIER = 31;
  private static final int WIDE_SHIFT = 16;
  private static final int HASH_ACCUMULATOR = 1;
  private static final int WIDE_TEMP = 2;

  private final Map<String, ClassGen> classes;
  private final TranslatorLogger logger;

  private RecordSupport(final Map<String, ClassGen> classes, final TranslatorLogger logger) {
    this.classes = classes;
    this.logger = logger;
  }

  public static void rewrite(final Map<String, ClassGen> classes, final TranslatorLogger logger) {
    new RecordSupport(classes, logger).rewriteRecords();
  }

  private void rewriteRecords() {
    for (final ClassGen classGen : this.classes.values()) {
      if (this.isRecordClass(classGen)) {
        this.rewriteRecord(classGen);
      }
    }
  }

  private void rewriteRecord(final ClassGen classGen) {
    final Method[] methods = classGen.getMethods();
    boolean rewritten = false;
    for (int index = 0; index < methods.length; index++) {
      if (!this.isCompilerObjectMethod(classGen, methods[index])) {
        continue;
      }
      classGen.setMethodAt(this.replacement(classGen, methods[index]), index);
      rewritten = true;
    }
    if (rewritten) {
      this.logger.logInfo("Rewrote compiler record methods of " + classGen.getClassName());
    }
  }

  private Method replacement(final ClassGen classGen, final Method method) {
    return switch (method.getName()) {
      case "equals" -> this.methodWith(classGen, method, this.equalsBody(classGen), 2);
      case "hashCode" -> this.methodWith(classGen, method, this.hashBody(classGen), 3);
      case "toString" -> this.methodWith(classGen, method, this.toStringBody(), 1);
      default -> throw new IllegalStateException(classGen.getClassName() + "#" + method.getName());
    };
  }

  private InstructionList equalsBody(final ClassGen classGen) {
    final InstructionFactory factory = new InstructionFactory(classGen);
    final InstructionList code = new InstructionList();
    final List<BranchInstruction> mismatches = new ArrayList<>();
    final String className = classGen.getClassName();

    code.append(InstructionConst.ALOAD_0);
    code.append(InstructionConst.ALOAD_1);
    final IF_ACMPEQ sameReference = new IF_ACMPEQ(null);
    code.append(sameReference);

    code.append(InstructionConst.ALOAD_1);
    code.append(factory.createInstanceOf(ObjectType.getInstance(className)));
    this.branchToMismatch(code, mismatches, new IFEQ(null));

    for (final Field field : this.instanceFields(classGen)) {
      this.appendEqualsComponent(code, factory, className, field, mismatches);
    }

    final InstructionHandle same = code.append(InstructionConst.ICONST_1);
    code.append(InstructionConst.IRETURN);
    final InstructionHandle different = code.append(InstructionConst.ICONST_0);
    code.append(InstructionConst.IRETURN);

    sameReference.setTarget(same);
    mismatches.forEach(branch -> branch.setTarget(different));
    return code;
  }

  private InstructionList hashBody(final ClassGen classGen) {
    final InstructionFactory factory = new InstructionFactory(classGen);
    final InstructionList code = new InstructionList();
    final String className = classGen.getClassName();

    code.append(InstructionConst.ICONST_1);
    code.append(InstructionConst.ISTORE_1);
    for (final Field field : this.instanceFields(classGen)) {
      code.append(InstructionConst.ILOAD_1);
      code.append(new BIPUSH((byte) HASH_MULTIPLIER));
      code.append(InstructionConst.IMUL);
      this.appendComponentHash(code, factory, className, field);
      code.append(InstructionConst.IADD);
      code.append(InstructionConst.ISTORE_1);
    }
    code.append(InstructionConst.ILOAD_1);
    code.append(InstructionConst.IRETURN);
    return code;
  }

  private InstructionList toStringBody() {
    final InstructionList code = new InstructionList();
    code.append(InstructionConst.ACONST_NULL);
    code.append(InstructionConst.ARETURN);
    return code;
  }

  private void appendEqualsComponent(
      final InstructionList code,
      final InstructionFactory factory,
      final String className,
      final Field field,
      final List<BranchInstruction> mismatches
  ) {
    final Type type = field.getType();
    if (this.isOneWord(type)) {
      this.appendGetField(code, factory, 0, className, field);
      this.appendGetField(code, factory, 1, className, field);
      this.branchToMismatch(code, mismatches, new IF_ICMPNE(null));
      return;
    }
    if (this.isWide(type)) {
      this.appendGetField(code, factory, 0, className, field);
      this.appendGetField(code, factory, 1, className, field);
      code.append(InstructionConst.LCMP);
      this.branchToMismatch(code, mismatches, new IFNE(null));
      return;
    }
    if (this.isRecordType(type)) {
      this.appendRecordEquals(code, factory, className, field, mismatches);
      return;
    }
    this.appendGetField(code, factory, 0, className, field);
    this.appendGetField(code, factory, 1, className, field);
    this.branchToMismatch(code, mismatches, new IF_ACMPNE(null));
  }

  private void appendRecordEquals(
      final InstructionList code,
      final InstructionFactory factory,
      final String className,
      final Field field,
      final List<BranchInstruction> mismatches
  ) {
    final String componentClass = field.getType().getClassName();

    this.appendGetField(code, factory, 0, className, field);
    this.appendGetField(code, factory, 1, className, field);
    final IF_ACMPEQ sameComponent = new IF_ACMPEQ(null);
    code.append(sameComponent);

    this.appendGetField(code, factory, 0, className, field);
    this.branchToMismatch(code, mismatches, new IFNULL(null));
    this.appendGetField(code, factory, 1, className, field);
    this.branchToMismatch(code, mismatches, new IFNULL(null));

    this.appendGetField(code, factory, 0, className, field);
    this.appendGetField(code, factory, 1, className, field);
    code.append(factory.createInvoke(componentClass, "equals", Type.BOOLEAN,
        new Type[] {Type.OBJECT}, INVOKEVIRTUAL));
    this.branchToMismatch(code, mismatches, new IFEQ(null));

    final InstructionHandle join = code.append(InstructionConst.NOP);
    sameComponent.setTarget(join);
  }

  private void appendComponentHash(
      final InstructionList code,
      final InstructionFactory factory,
      final String className,
      final Field field
  ) {
    final Type type = field.getType();
    if (this.isOneWord(type)) {
      this.appendGetField(code, factory, 0, className, field);
      return;
    }
    if (this.isWide(type)) {
      this.appendWideHash(code, factory, className, field);
      return;
    }
    this.appendReferenceHash(code, factory, className, field);
  }

  private void appendWideHash(
      final InstructionList code,
      final InstructionFactory factory,
      final String className,
      final Field field
  ) {
    this.appendGetField(code, factory, 0, className, field);
    code.append(InstructionConst.DUP2);
    code.append(InstructionConst.L2I);
    code.append(new ISTORE(WIDE_TEMP));
    code.append(new BIPUSH((byte) WIDE_SHIFT));
    code.append(InstructionConst.LUSHR);
    code.append(InstructionConst.L2I);
    code.append(new ILOAD(WIDE_TEMP));
    code.append(InstructionConst.IXOR);
  }

  private void appendReferenceHash(
      final InstructionList code,
      final InstructionFactory factory,
      final String className,
      final Field field
  ) {
    final String hashClass = this.isRecordType(field.getType())
        ? field.getType().getClassName()
        : "java.lang.Object";

    this.appendGetField(code, factory, 0, className, field);
    code.append(InstructionConst.DUP);
    final IFNONNULL present = new IFNONNULL(null);
    code.append(present);
    code.append(InstructionConst.POP);
    code.append(InstructionConst.ICONST_0);
    final GOTO done = new GOTO(null);
    code.append(done);

    final InstructionHandle call = code.append(factory.createInvoke(
        hashClass, "hashCode", Type.INT, Type.NO_ARGS, INVOKEVIRTUAL));
    present.setTarget(call);
    final InstructionHandle join = code.append(InstructionConst.NOP);
    done.setTarget(join);
  }

  private void appendGetField(
      final InstructionList code,
      final InstructionFactory factory,
      final int local,
      final String className,
      final Field field
  ) {
    code.append(new ALOAD(local));
    code.append(factory.createGetField(className, field.getName(), field.getType()));
  }

  private void branchToMismatch(
      final InstructionList code,
      final List<BranchInstruction> mismatches,
      final BranchInstruction branch
  ) {
    mismatches.add(branch);
    code.append(branch);
  }

  private Method methodWith(
      final ClassGen classGen,
      final Method method,
      final InstructionList body,
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
      methodGen.removeNOPs();
      methodGen.setMaxStack(8);
      methodGen.setMaxLocals(locals);
      return methodGen.getMethod();
    } finally {
      body.dispose();
    }
  }

  private boolean isCompilerObjectMethod(final ClassGen classGen, final Method method) {
    if (!method.isFinal() || method.isStatic() ||
        !this.isObjectMethodSignature(method.getName(), method.getSignature())) {
      return false;
    }
    final MethodGen methodGen =
        new MethodGen(method, classGen.getClassName(), classGen.getConstantPool());
    final InstructionList instructions = methodGen.getInstructionList();
    if (instructions == null) {
      return false;
    }
    for (InstructionHandle handle = instructions.getStart(); handle != null;
         handle = handle.getNext()) {
      if (!(handle.getInstruction() instanceof INVOKEDYNAMIC invokedynamic)) {
        continue;
      }
      if (method.getName().equals(invokedynamic.getMethodName(classGen.getConstantPool()))) {
        return true;
      }
    }
    return false;
  }

  private boolean isObjectMethodSignature(final String name, final String signature) {
    return switch (name) {
      case "equals" -> "(Ljava/lang/Object;)Z".equals(signature);
      case "hashCode" -> "()I".equals(signature);
      case "toString" -> "()Ljava/lang/String;".equals(signature);
      default -> false;
    };
  }

  private List<Field> instanceFields(final ClassGen classGen) {
    return Arrays.stream(classGen.getFields())
        .filter(field -> !field.isStatic())
        .collect(toList());
  }

  private boolean isRecordClass(final ClassGen classGen) {
    return classGen != null && RECORD_CLASS.equals(classGen.getSuperclassName());
  }

  private boolean isRecordType(final Type type) {
    if (!(type instanceof ObjectType)) {
      return false;
    }
    return this.isRecordClass(this.classes.get(type.getClassName()));
  }

  private boolean isOneWord(final Type type) {
    final byte kind = type.getType();
    return kind == Type.BOOLEAN.getType()
        || kind == Type.BYTE.getType()
        || kind == Type.CHAR.getType()
        || kind == Type.SHORT.getType()
        || kind == Type.INT.getType()
        || kind == Type.FLOAT.getType();
  }

  private boolean isWide(final Type type) {
    final byte kind = type.getType();
    return kind == Type.LONG.getType() || kind == Type.DOUBLE.getType();
  }
}
