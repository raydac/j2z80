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

package com.igormaznitsa.j2z80;

import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.MethodID;
import java.io.IOException;
import java.util.List;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.generic.Type;

/**
 * Describes the translator state and all requested runtime services needed while compiling a
 * Java class hierarchy into Z80 assembly. Implementations keep the class/method registry,
 * translation metadata, and generated resource references together so every code-generation step
 * can inspect the same execution context.
 *
 * <p>This interface is the integration point between the translator core and bootstrap classes,
 * generated resource labels, and runtime fragments such as heap management and virtual dispatch
 * tables.</p>
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface TranslatorContext {
  /**
   * Reserved Java entry point name used by the Z80 translator for the generated main routine.
   */
  String Z80_MAIN_METHOD_NAME = "mainz";

  /**
   * JVM signature of a zero-argument {@code void} method.
   */
  String Z80_MAIN_METHOD_SIGNATURE = Type.getMethodSignature(Type.VOID, new Type[0]);

  /**
   * Translates a Java application or library entry point into a list of generated assembly
   * source lines.
   *
   * @param mainClassName the fully qualified entry class name, or {@code null} when the
   *                      caller is translating a class without a Java {@code main} entry
   * @param startAddress the memory address where the generated code block begins in the final
   *                     image
   * @param stackTopAddress the top-of-stack address reserved for the translated program
   * @param excludeBinResPatterns a list of Ant-style patterns used to suppress embedded binary
   *                              resources from the generated output
   * @param bootstrapClassLoader the class loader used to resolve bootstrap classes and runtime
   *                            support types
   * @return the generated Z80 assembly source as one line per statement
   * @throws IOException if a class or resource cannot be loaded or read during translation
   */
  List<String> translate(String mainClassName, int startAddress, int stackTopAddress,
                         List<String> excludeBinResPatterns, ClassLoader bootstrapClassLoader)
      throws IOException;

  /**
   * Returns the current class registry used during translation.
   *
   * @return the active class context; never {@code null}
   */
  ClassContext getClassContext();

  /**
   * Returns the method registry used during translation.
   *
   * @return the active method context; never {@code null}
   */
  MethodContext getMethodContext();

  /**
   * Returns the logger attached to this translator instance.
   *
   * @return the configured logger; never {@code null}
   */
  TranslatorLogger getLogger();

  /**
   * Registers the translator additions required by a Java class. This allows the runtime to
   * include the appropriate assembly manager for features such as memory management, checked
   * exceptions, or virtual dispatch.
   *
   * @param classToCheck the class whose declaration is examined for additional runtime blocks
   */
  void registerAdditionsUsedByClass(Class<?> classToCheck);

  /**
   * Registers a class identifier for later use in cast and instance checks.
   *
   * @param classId the class identity to register, must not be {@code null}
   * @return the generated class identifier, or {@code null} if the class is not known to the
   *         translator yet
   */
  Integer registerClassForCastCheck(ClassID classId);

  /**
   * Registers a virtual method identifier used by {@code INVOKEINTERFACE} dispatch.
   *
   * @param methodId the method identifier to register, must not be {@code null}
   * @return the generated interface method identifier, or {@code null} if one is not available
   */
  Integer registerInterfaceMethodForINVOKEINTERFACE(MethodID methodId);

  /**
   * Registers a constant-pool item that needs a generated label in the final assembly.
   *
   * @param constantLabel the unique label assigned to the constant, must not be {@code null}
   * @param item the constant pool entry to be emitted into the output, must not be {@code null}
   */
  void registerConstantPoolItem(String constantLabel, Constant item);

  /**
   * Registers a ROM-resident {@code byte[]} array so the translator can emit a static table that
   * is stored in the program image rather than allocated on the heap.
   *
   * @param data the payload bytes to embed, must not be {@code null}
   * @return the generated assembler label for the first element in the array
   */
  String registerStaticByteArrayTemplate(byte[] data);

  /**
   * Registers a bootstrap processor so its additional assembly fragments can be included in the
   * generated program.
   *
   * @param classProcessor the bootstrap class processor to register, must not be {@code null}
   */
  void registerCalledBootClassProcesser(AbstractBootstrapClass classProcessor);

  /**
   * Loads a resource from the translator's virtual resource space.
   *
   * @param path the resource path inside the translation package, must not be {@code null}
   * @return the resource contents as raw bytes, or {@code null} if no matching resource exists
   * @throws IOException if the resource cannot be read
   */
  byte[] loadResourceForPath(final String path) throws IOException;

}
