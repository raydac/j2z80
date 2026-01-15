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

import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;

/**
 * The class allows to parse a Jar file and extract its entries.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ZParsedJar {

  private final File file;
  private final JarFile jarFile;

  private final List<ClassGen> classes = new ArrayList<>();
  private final Map<String, byte[]> nativeCodeFiles = new HashMap<>();
  private final Map<String, byte[]> binaryResources = new HashMap<>();

  public ZParsedJar(final File jarFile) {
    this.file = jarFile;
    try {
      this.jarFile = new JarFile(jarFile);
      this.extractAll();
    } catch (IOException ex) {
      throw new RuntimeException("Can't extract jar file: " + jarFile);
    }
  }

  public static String normalizeEntryPath(final String path) {
    String name = path;
    name = name.replace('\\', '/');
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    return name;
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

  private void extractAll() throws IOException {
    final Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      final JarEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        final byte[] entryData = extractEntry(entry);
        final String name = normalizeEntryPath(entry.getName());
        if (isJavaClass(entry)) {
          final ClassGen classGen = new ClassGen(
              new ClassParser(new ByteArrayInputStream(entryData), entry.getName()).parse());
          final String message = ClassValidator.validateClass(classGen);
          if (message != null) {
            throw new IOException(
                "Disallowed class detected " + classGen.getClassName() + " [" + message + ']');
          }
          classes.add(classGen);
        } else if (isNativeCodeFile(entry)) {
          nativeCodeFiles.put(name, entryData);
        } else {
          binaryResources.put(name, entryData);
        }
      }
    }
  }

  public List<ClassGen> getAllJavaClasses() {
    return Collections.unmodifiableList(classes);
  }

  public Map<String, byte[]> getAllJNIData() {
    return Collections.unmodifiableMap(nativeCodeFiles);
  }

  public Map<String, byte[]> getAllBinaryResources() {
    return Collections.unmodifiableMap(binaryResources);
  }

  private boolean isJavaClass(final JarEntry entry) {
    return !entry.isDirectory() && entry.getName().endsWith(".class");
  }

  private boolean isNativeCodeFile(final JarEntry entry) {
    if (!entry.isDirectory()) {
      final String entryName = entry.getName();
      return ClassUtils.isAllowedNativeSourceCodeName(entryName) ||
          ClassUtils.isAllowedCompiledNativeCodeName(entryName);
    }
    return false;
  }

  public byte[] extractEntry(final JarEntry entry) throws IOException {
    int length = (int) entry.getSize();
    final byte[] buffer = new byte[(int) entry.getSize()];
    final InputStream inStream = jarFile.getInputStream(entry);
    try {
      int pos = 0;
      while (length > 0) {
        final int read = inStream.read(buffer, pos, length);
        pos += read;
        length -= read;
      }
    } finally {
      Utils.silentlyClose(inStream);
    }
    return buffer;
  }

}
