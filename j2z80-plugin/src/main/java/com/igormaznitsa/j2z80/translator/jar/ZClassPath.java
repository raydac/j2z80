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
package com.igormaznitsa.j2z80.translator.jar;

import static java.util.Collections.unmodifiableMap;

import com.igormaznitsa.j2z80.TranslatorContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;

/**
 * The class implements inside virtual class and resource storage for the translator.
 * The Storage is formed through parsing all jar files and extract all their classes and resources.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ZClassPath {

  private final Map<String, ClassGen> classStorage = new HashMap<>();
  private final Map<String, byte[]> jniCodeStorage = new HashMap<>();
  private final Map<String, byte[]> binaryDataStorage = new HashMap<>();
  private final TranslatorContext context;
  private final List<ZParsedJar> jarFiles;
  private ClassGen mainClass;
  private Method mainMethod;

  public ZClassPath(final TranslatorContext context, final List<ZParsedJar> classPath) {
    this.context = context;
    this.jarFiles = classPath;
    classPath.forEach(this::processArchive);
  }

  @Override
  public String toString() {
    return Stream.of(this.jarFiles).map(Object::toString).collect(Collectors.joining(
        File.pathSeparator));
  }

  public ClassGen findMainClass(final String mainClassName, final String mainMethod, final String mainMethodSignature) {
    if (mainClassName != null) {
      this.mainClass = classStorage.get(mainClassName);
      if (this.mainClass != null) {
        this.mainMethod = findMainMethodInClass(mainClass, mainMethod, mainMethodSignature);
      }
      return this.mainClass;
    }

    final List<ZParsedJar> reversed = new ArrayList<>(this.jarFiles);
    Collections.reverse(reversed);

    return reversed.stream().flatMap(processingJar -> {
      for (final ClassGen classGen : processingJar.getAllJavaClasses()) {
        final Method foundMethod = findMainMethodInClass(classGen, mainMethod, mainMethodSignature);
        if (foundMethod != null && isClassPresented(classGen)) {
          this.mainClass = classGen;
          this.mainMethod = foundMethod;
          return Stream.of(this.mainClass);
        }
      }
      return Stream.empty();
    }).findFirst().orElse(null);
  }

  private boolean isClassPresented(final ClassGen classData) {
    for (final ClassGen cgen : classStorage.values()) {
      if (cgen == classData) {
        return true;
      }
    }
    return false;
  }

  private Method findMainMethodInClass(final ClassGen classGen, final String mainMethodName, final String mainMethodSignature) {
    if (!classGen.isInterface() && !classGen.isEnum()) {
      for (final Method method : classGen.getMethods()) {
        if (method.isStatic() && mainMethodName.equals(method.getName()) && mainMethodSignature.equals(method.getSignature())) {
          return method;
        }
      }
    }
    return null;
  }

  public ClassGen getMainClass() {
    return mainClass;
  }

  public Method getMainMethod() {
    return mainMethod;
  }

  private void processArchive(final ZParsedJar archive) {
    processClasses(archive);
    processJniCode(archive);
    processBinaryResource(archive);
  }

  private void processClasses(final ZParsedJar archive) {
    for (final ClassGen classData : archive.getAllJavaClasses()) {
      final String className = classData.getClassName();

      if (classStorage.containsKey(className)) {
        context.getLogger().logWarning("Detected overridden class " + className);
      }
      classStorage.put(className, classData);
    }
  }

  private void processJniCode(final ZParsedJar archive) {
    for (final Entry<String, byte[]> jniData : archive.getAllJNIData().entrySet()) {
      final String resName = jniData.getKey();
      if (jniCodeStorage.containsKey(resName)) {
        context.getLogger().logWarning("Detected overridden JNI resource " + resName);
      }
      jniCodeStorage.put(resName, jniData.getValue());
    }
  }

  private void processBinaryResource(final ZParsedJar archive) {
    for (final Entry<String, byte[]> binData : archive.getAllBinaryResources().entrySet()) {
      final String resName = binData.getKey();
      if (binaryDataStorage.containsKey(resName)) {
        this.context.getLogger().logWarning("Detected overridden binary resource " + resName);
      }
      this.binaryDataStorage.put(resName, binData.getValue());
    }
  }

  public byte[] findJniCodeForPath(final String path) {
    return this.jniCodeStorage.get(path);
  }

  public byte[] findNonClassForPath(final String path) {
    byte[] result = this.jniCodeStorage.get(ZParsedJar.normalizeEntryPath(path));
    if (result == null) {
      result = this.binaryDataStorage.get(path);
    }
    return result;
  }

  public Map<String, ClassGen> getAllClasses() {
    return unmodifiableMap(this.classStorage);
  }

  public ClassGen findClassForName(final String name) {
    return this.classStorage.get(name);
  }

  public Map<String, byte[]> getAllBinaryResources() {
    return unmodifiableMap(this.binaryDataStorage);
  }
}
