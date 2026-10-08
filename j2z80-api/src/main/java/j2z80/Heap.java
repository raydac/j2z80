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

package j2z80;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.bootstrap.BootClassException;
import java.util.List;
import org.apache.bcel.generic.Type;

/**
 * Provides low-level heap management for programs translated by the j2z80 runtime. The heap uses a
 * simple bump-pointer model: objects are allocated from a single growing address space, and
 * {@link #forget(Object)} can rewind the pointer back to a previously live instance boundary.
 *
 * <p>This type is intentionally small and intentionally exposes only the few operations a translated
 * Java program needs when it creates and discards short-lived objects on the target platform.</p>
 *
 * @since 2.0.0
 */
public final class Heap extends AbstractBootstrapClass {

  /**
   * Rewinds the heap back to the memory address it had before {@code instance} was created.
   * Every object allocated after that instance is released as part of the same operation; a
   * {@code null} reference is treated as a no-op.
   *
   * <p>Use this for short-lived temporary graphs that should be discarded in one step. It does not
   * perform a tracing garbage collection; it simply moves the bump pointer backwards to the object
   * header of the supplied instance.</p>
   *
   * @param instance the root object to forget, or {@code null} to leave the heap unchanged
   * @since 2.0.0
   */
  public static void forget(final Object instance) {
  }

  /**
   * Returns the current top-of-heap address, i.e. the location where the next object will be
   * allocated. The returned value is expressed as a signed 16-bit address, matching the same form
   * used by object references on the target platform.
   *
   * @return the current heap pointer as a signed 16-bit memory address
   * @since 2.0.0
   */
  public static int top() {
    return 0;
  }

  @Override
  public boolean doesInvokeNeedFrame(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    return false;
  }

  @Override
  public List<String> generateInvocation(final TranslatorContext translator,
                                         final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    if (methodName.equals("forget")
        && methodArguments.length == 1
        && methodArguments[0].getType() == Type.OBJECT.getType()
        && resultType.getType() == Type.VOID.getType()) {
      translator.registerAdditionsUsedByClass(MemoryForget.class);
      return List.of(
          "POP BC ; Heap.forget, the instance reference",
          "CALL " + NeedsMemoryManager.SUB_FORGET_OBJECT
      );
    }
    if (methodName.equals("top")
        && methodArguments.length == 0
        && resultType.getType() == Type.INT.getType()) {
      translator.registerAdditionsUsedByClass(MemoryForget.class);
      return List.of(
          "LD BC,(" + NeedsMemoryManager.VAR_MANAGER_TOP_POINTER + ")",
          "PUSH BC ; Heap.top, the first free heap address"
      );
    }
    final String signature = Type.getMethodSignature(resultType, methodArguments);
    throw new BootClassException("Unsupported method: j2z80.Heap." + methodName + " " + signature,
        "j2z80.Heap", methodName, signature);
  }

  @Override
  public List<String> generateFieldGetter(final TranslatorContext context, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.Heap." + fieldName,
        "j2z80.Heap", fieldName, fieldType.getSignature());
  }

  @Override
  public List<String> generateFieldSetter(final TranslatorContext context, final String fieldName,
                                          final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.Heap." + fieldName,
        "j2z80.Heap", fieldName, fieldType.getSignature());
  }

  private static final class MemoryForget implements NeedsMemoryManager {
  }
}
