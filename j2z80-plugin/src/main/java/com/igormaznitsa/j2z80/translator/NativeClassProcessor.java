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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import org.apache.bcel.classfile.Method;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class allows to process classes containing JNI methods.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class NativeClassProcessor {

  private final TranslatorContext theTranslator;

  public NativeClassProcessor(final TranslatorContext translator) {
    this.theTranslator = translator;
  }

  private static String[] insertFirstStringIntoArray(final String str, final String[] array) {
    final String[] result = new String[array.length + 1];
    System.arraycopy(array, 0, result, 1, array.length);
    result[0] = str;
    return result;
  }

  public String[] findNativeSources(final ClassMethodInfo classInfo) throws IOException {
    final String packageName = classInfo.getPackageName();
    final String onlyClassName = classInfo.getOnlyClassName();
    final String path = packageName.replace('.', '/');

    // find the whole class
    final String[] jniWholeClass = readNativeResource(path, onlyClassName);

    // find resources for each native method
    final Map<Method, String[]> jniMethodBodies = new HashMap<>();
    final Set<String> jniMethodNames = new HashSet<>();
    for (final Method method : classInfo.getClassInfo().getMethods()) {
      if (!method.isNative()) {
        continue;
      }
      final String methodName = method.getName();

      final String resourceName = onlyClassName + '#' + methodName;

      final String[] methodBody = readNativeResource(path, resourceName);
      if (methodBody != null) {
        if (jniMethodNames.contains(methodName)) {
          final String errorMessage = "Found two or more JNI methods in " + classInfo.getCanonicalClassName() + " named " + methodName + " and one of them has the separated JNI file as the body. In the case the class must have only JNI method with the name.";
          theTranslator.getLogger().logError(errorMessage);
        }
        jniMethodBodies.put(method, methodBody);
        jniMethodNames.add(methodName);
      }
    }

    // make the result
    // as the first we add the whole class body if it is presented
    final List<String> result = new ArrayList<>(1024);

    result.add("");
    result.add("; -------- [JNI] START OF " + classInfo.getCanonicalClassName() + " --------");
    if (jniWholeClass != null) {
      result.addAll(Arrays.asList(jniWholeClass));
    }

    // next we add all found method bodies and generate labels for them
    if (!jniMethodBodies.isEmpty()) {
      for (final Map.Entry<Method, String[]> methodEntry : jniMethodBodies.entrySet()) {
        final Method method = methodEntry.getKey();
        final String methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(classInfo.getClassInfo().getClassName(), method.getName(), method.getReturnType(), method.getArgumentTypes());

        result.add(methodLabel + ':');

        result.addAll(Arrays.asList(methodEntry.getValue()));
      }
    }
    result.add("; -------- [JNI] END OF " + classInfo.getCanonicalClassName() + " --------");
    result.add("");
    return result.toArray(new String[0]);
  }

  private String[] readNativeResource(final String path, final String resourceName) throws IOException {
    byte[] result = null;
    final String filePath = path + '/' + resourceName;

    String readResourcePath = null;

    // find between asm files
    for (final String ext : ClassUtils.ALLOWED_JNI_ASM_EXTENSIONS) {
      readResourcePath = filePath + ext;
      result = theTranslator.loadResourceForPath(readResourcePath);
      if (result != null) {
        break;
      }
    }

    if (result != null) {
      return insertFirstStringIntoArray("; file " + readResourcePath, Utils.breakToLines(new String(result, StandardCharsets.UTF_8)));
    }

    // find between bin files
    for (final String ext : ClassUtils.ALLOWED_JNI_BIN_EXTENSIONS) {
      readResourcePath = filePath + ext;
      result = theTranslator.loadResourceForPath(readResourcePath);
      if (result != null) {
        break;
      }
    }

    if (result != null) {
      return Utils.byteArrayToAsm("; file " + readResourcePath, result, -1);
    }

    return null;
  }
}
