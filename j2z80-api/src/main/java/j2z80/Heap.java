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

package j2z80;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.bootstrap.BootClassException;
import org.apache.bcel.generic.Type;

/**
 * Bump-heap operations for a program compiled against the JDK.
 *
 * @since 2.0.0
 */
public final class Heap extends AbstractBootstrapClass {

  /**
   * Rewinds the bump heap to the address it had before {@code instance} was created.
   * That instance and every instance allocated after it are released. A {@code null} reference
   * does nothing.
   *
   * @since 2.0.0
   */
  public static void forget(final Object instance) {
  }

  /**
   * Returns the bump pointer, the address where the next instance will be allocated.
   * The value is a signed 16-bit address, the same form {@code hashCode()} uses.
   *
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
  public String[] generateInvocation(final TranslatorContext translator, final String methodName,
                                     final Type[] methodArguments, final Type resultType) {
    if (methodName.equals("forget")
        && methodArguments.length == 1
        && methodArguments[0].getType() == Type.OBJECT.getType()
        && resultType.getType() == Type.VOID.getType()) {
      translator.registerAdditionsUsedByClass(MemoryForget.class);
      return new String[] {
          "POP BC ; Heap.forget, the instance reference",
          "CALL " + NeedsMemoryManager.SUB_FORGET_OBJECT
      };
    }
    if (methodName.equals("top")
        && methodArguments.length == 0
        && resultType.getType() == Type.INT.getType()) {
      translator.registerAdditionsUsedByClass(MemoryForget.class);
      return new String[] {
          "LD BC,(" + NeedsMemoryManager.VAR_MANAGER_TOP_POINTER + ")",
          "PUSH BC ; Heap.top, the first free heap address"
      };
    }
    final String signature = Type.getMethodSignature(resultType, methodArguments);
    throw new BootClassException("Unsupported method: j2z80.Heap." + methodName + " " + signature,
        "j2z80.Heap", methodName, signature);
  }

  @Override
  public String[] generateFieldGetter(final TranslatorContext context, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.Heap." + fieldName,
        "j2z80.Heap", fieldName, fieldType.getSignature());
  }

  @Override
  public String[] generateFieldSetter(final TranslatorContext context, final String fieldName,
                                      final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.Heap." + fieldName,
        "j2z80.Heap", fieldName, fieldType.getSignature());
  }

  private static final class MemoryForget implements NeedsMemoryManager {
  }
}
