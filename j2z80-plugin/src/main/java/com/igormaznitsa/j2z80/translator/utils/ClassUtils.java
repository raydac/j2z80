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

package com.igormaznitsa.j2z80.translator.utils;

import static com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass.J2Z80_BOOTSTRAP_PACKAGE_PREFIX;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.api.additional.J2Z80Ignore;
import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.jar.ZClassPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.RuntimeVisibleAnnotations;
import org.apache.bcel.generic.ClassGen;

public enum ClassUtils {
  ;

  public static final String[] ALLOWED_JNI_ASM_EXTENSIONS = new String[] {".z80", ".a80", ".asm"};
  public static final String[] ALLOWED_JNI_BIN_EXTENSIONS = new String[] {".bin"};
  private static final String
      IGNORE_ANNOTATION = "L" + J2Z80Ignore.class.getCanonicalName().replace('.', '/') + ';';

  public static boolean isAllowedNativeSourceCodeName(final String name) {
    for (final String ext : ALLOWED_JNI_ASM_EXTENSIONS) {
      if (name.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isAllowedCompiledNativeCodeName(final String name) {
    for (final String ext : ALLOWED_JNI_BIN_EXTENSIONS) {
      if (name.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasJ2Z80Ignore(final Attribute[] attributes) {
    if (attributes == null) {
      return false;
    }
    for (final Attribute attr : attributes) {
      if (attr instanceof RuntimeVisibleAnnotations) {
        for (final AnnotationEntry ae : ((RuntimeVisibleAnnotations) attr).getAnnotationEntries()) {
          if (ae.getAnnotationType().equals(IGNORE_ANNOTATION)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isJ2Z80ObjectClass(final String className) {
    if (className.equals("java.lang.Object")) {
      return true;
    }
    return (J2Z80_BOOTSTRAP_PACKAGE_PREFIX + '.' +
        "java.lang.Object").equals(
        className);
  }

  public static List<Field> findAllFields(final ZClassPath archive, final ClassGen classGen) {
    final List<Field> result = new ArrayList<>();
    _fillAllFields(archive, classGen, result);
    return result;
  }

  private static void _fillAllFields(final ZClassPath archive, final ClassGen classGen,
                                     final List<Field> result) {
    for (final Field f : classGen.getFields()) {
      if (!f.isStatic()) {
        if (ClassUtils.isInJ2Z80BootstrapPackage(classGen)) {
          if (f.isPublic()) {
            result.add(f);
          }
        } else {
          result.add(f);
        }
      }
    }

    final String superclass = classGen.getSuperclassName();

    if (isJ2Z80ObjectClass(superclass)) {
      return;
    }

    final ClassGen superClass = archive.findClassForName(superclass);
    if (superClass == null) {
      throw new IllegalArgumentException(
          "Unknown superclass detected [" + classGen.getClassName() + " extends " + superclass +
              "]");
    }
    _fillAllFields(archive, superClass, result);
  }

  public static Set<Class<? extends J2ZAdditionalBlock>> findAllAdditionalBlocksInClass(
      final Class<?> processingClass) {
    final Set<Class<? extends J2ZAdditionalBlock>> result = new HashSet<>();
    _findAllAdditionalBlocksInClass(processingClass, result);
    return result;
  }

  private static void _findAllAdditionalBlocksInClass(final Class<?> processor,
                                                      final Set<Class<? extends J2ZAdditionalBlock>> result) {
    for (final Class<?> type : processor.getInterfaces()) {
      if (J2ZAdditionalBlock.class.isAssignableFrom(type)) {
        result.add(type.asSubclass(J2ZAdditionalBlock.class));
      }
    }

    final Class<?> superclass = processor.getSuperclass();
    if (superclass != Object.class) {
      _findAllAdditionalBlocksInClass(superclass, result);
    }
  }

  public static Set<ClassID> findAllImplementedInterfaces(final ClassContext classContext,
                                                          final String className) {
    final Set<ClassID> result = new HashSet<>();

    final ClassGen classGen = classContext.findClassForID(new ClassID(className));

    for (final String interfaceName : classGen.getInterfaceNames()) {
      result.add(new ClassID(interfaceName));
      result.addAll(findAllImplementedInterfaces(classContext, interfaceName));
    }

    return result;
  }


  public static int calculateInstanceSize(final ZClassPath classPath, final ClassGen classGen) {
    if (classGen.isInterface() || classGen.isEnum() || classGen.isAnnotation() ||
        classGen.isAbstract()) {
      return 0;
    }

    final String superClass = classGen.getSuperclassName();

    int fieldNumber = 0;

    for (final Field fld : classGen.getFields()) {
      if (fld.isStatic()) {
        continue;
      }

      switch (fld.getType().getType()) {
        case Const.T_DOUBLE:
        case Const.T_FLOAT:
        case Const.T_LONG:
          throw new IllegalStateException(
              "Class " + classGen.getClassName() + " contains inappropriate field [" +
                  fld.toString() + ']');
        default: {
          fieldNumber++;
        }
        break;
      }


      fieldNumber++;
    }

    if (isJ2Z80ObjectClass(superClass)) {
      return (fieldNumber << 1);
    }

    final ClassGen superClassGen = classPath.findClassForName(superClass);
    if (superClassGen == null) {
      throw new IllegalStateException(
          "Not found superclass " + superClass + " for " + classGen.getClassName());
    }

    return (fieldNumber << 1) + calculateInstanceSize(classPath, superClassGen);
  }

  public static Method[] findBoostrapAwareMethods(final ClassGen classGen) {
    if (isInJ2Z80BootstrapPackage(classGen)) {
      return Arrays.stream(classGen.getMethods())
          .filter(x -> {
            final String name = x.getName();
            if (name.startsWith("<")) {
              return false;
            }
            for (final java.lang.reflect.Method m : AbstractBootstrapClass.class.getDeclaredMethods()) {
              if (m.getName().equals(name)) {
                return false;
              }
            }
            return true;
          }).toArray(Method[]::new);
    } else {
      return classGen.getMethods();
    }
  }

  public static boolean isInJ2Z80BootstrapPackage(ClassGen classGen) {
    return classGen.getClassName().startsWith(J2Z80_BOOTSTRAP_PACKAGE_PREFIX + '.');
  }
}
