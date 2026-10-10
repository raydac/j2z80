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
 * Provides low-level system operations for programs translated by the j2z80 runtime, including
 * heap management, memory access, processor control, and I/O.
 *
 * <p>These operations map directly to the small runtime instruction sequences needed by translated
 * Java programs.</p>
 *
 * @since 2.0.0
 */
public final class ZSystem extends AbstractBootstrapClass {

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

  /**
   * Writes a byte to the address selected by the low 16 bits of {@code address}.
   *
   * @param address the address, interpreted as an unsigned 16-bit value
   * @param value   the byte to write
   * @since 2.0.0
   */
  public static void poke(final int address, final byte value) {
  }

  /**
   * Reads a signed byte from the address selected by the low 16 bits of {@code address}.
   *
   * @param address the address, interpreted as an unsigned 16-bit value
   * @return the byte at the address
   * @since 2.0.0
   */
  public static byte peek(final int address) {
    return 0;
  }

  /**
   * Stops the Z80 processor.
   *
   * @since 2.0.0
   */
  public static void halt() {
  }

  /**
   * Reads a byte from the port selected by the low 16 bits of {@code port}.
   *
   * @param port the port, interpreted as an unsigned 16-bit value
   * @return the signed byte read from the port
   * @since 2.0.0
   */
  public static byte in(final int port) {
    return 0;
  }

  /**
   * Writes a byte to the port selected by the low 16 bits of {@code port}.
   *
   * @param port  the port, interpreted as an unsigned 16-bit value
   * @param value the byte to write
   * @since 2.0.0
   */
  public static void out(final int port, final byte value) {
  }

  /**
   * Sets the Z80 interrupt mode. Values other than 0, 1, or 2 leave the current mode unchanged.
   *
   * @param mode the interrupt mode, 0, 1, or 2
   * @since 2.0.0
   */
  public static void im(final int mode) {
  }

  /**
   * Enables maskable interrupts.
   *
   * @since 2.0.0
   */
  public static void ei() {
  }

  /**
   * Disables maskable interrupts.
   *
   * @since 2.0.0
   */
  public static void di() {
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
          "POP BC ; ZSystem.forget, the instance reference",
          "CALL " + NeedsMemoryManager.SUB_FORGET_OBJECT
      );
    }
    if (methodName.equals("top")
        && methodArguments.length == 0
        && resultType.getType() == Type.INT.getType()) {
      translator.registerAdditionsUsedByClass(MemoryForget.class);
      return List.of(
          "LD BC,(" + NeedsMemoryManager.VAR_MANAGER_TOP_POINTER + ")",
          "PUSH BC ; ZSystem.top, the first free heap address"
      );
    }
    if (methodName.equals("poke")
        && methodArguments.length == 2
        && methodArguments[0].getType() == Type.INT.getType()
        && methodArguments[1].getType() == Type.BYTE.getType()
        && resultType.getType() == Type.VOID.getType()) {
      return List.of(
          "POP DE",
          "POP HL",
          "LD (HL),E"
      );
    }
    if (methodName.equals("peek")
        && methodArguments.length == 1
        && methodArguments[0].getType() == Type.INT.getType()
        && resultType.getType() == Type.BYTE.getType()) {
      return List.of(
          "POP HL",
          "LD A,(HL)",
          "LD C,A",
          "ADD A,A",
          "SBC A,A",
          "LD B,A",
          "PUSH BC"
      );
    }
    if (methodName.equals("halt")
        && methodArguments.length == 0
        && resultType.getType() == Type.VOID.getType()) {
      return List.of("HALT");
    }
    if (methodName.equals("in")
        && methodArguments.length == 1
        && methodArguments[0].getType() == Type.INT.getType()
        && resultType.getType() == Type.BYTE.getType()) {
      return List.of(
          "POP BC",
          "IN A,(C)",
          "LD C,A",
          "ADD A,A",
          "SBC A,A",
          "LD B,A",
          "PUSH BC"
      );
    }
    if (methodName.equals("out")
        && methodArguments.length == 2
        && methodArguments[0].getType() == Type.INT.getType()
        && methodArguments[1].getType() == Type.BYTE.getType()
        && resultType.getType() == Type.VOID.getType()) {
      return List.of(
          "POP DE",
          "POP BC",
          "LD A,E",
          "OUT (C),A"
      );
    }
    if (methodName.equals("im")
        && methodArguments.length == 1
        && methodArguments[0].getType() == Type.INT.getType()
        && resultType.getType() == Type.VOID.getType()) {
      return List.of(
          "POP BC",
          "CALL ___ZSYSTEM_SET_INTERRUPT_MODE"
      );
    }
    if ((methodName.equals("ei") || methodName.equals("di"))
        && methodArguments.length == 0
        && resultType.getType() == Type.VOID.getType()) {
      return List.of(methodName.toUpperCase());
    }
    final String signature = Type.getMethodSignature(resultType, methodArguments);
    throw new BootClassException(
        "Unsupported method: j2z80.ZSystem." + methodName + " " + signature,
        "j2z80.ZSystem", methodName, signature);
  }

  @Override
  public List<String> getAdditionalText() {
    return List.of(
        "___ZSYSTEM_SET_INTERRUPT_MODE:",
        "LD A,B",
        "OR A",
        "JR NZ,___ZSYSTEM_SET_INTERRUPT_MODE_DONE",
        "LD A,C",
        "CP 0",
        "JR Z,___ZSYSTEM_SET_INTERRUPT_MODE_0",
        "CP 1",
        "JR Z,___ZSYSTEM_SET_INTERRUPT_MODE_1",
        "CP 2",
        "JR Z,___ZSYSTEM_SET_INTERRUPT_MODE_2",
        "___ZSYSTEM_SET_INTERRUPT_MODE_DONE:",
        "RET",
        "___ZSYSTEM_SET_INTERRUPT_MODE_0:",
        "IM 0",
        "RET",
        "___ZSYSTEM_SET_INTERRUPT_MODE_1:",
        "IM 1",
        "RET",
        "___ZSYSTEM_SET_INTERRUPT_MODE_2:",
        "IM 2",
        "RET"
    );
  }

  @Override
  public List<String> generateFieldGetter(final TranslatorContext context, final String fieldName,
                                          final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.ZSystem." + fieldName,
        "j2z80.ZSystem", fieldName, fieldType.getSignature());
  }

  @Override
  public List<String> generateFieldSetter(final TranslatorContext context, final String fieldName,
                                          final Type fieldType, final boolean isStatic) {
    throw new BootClassException("Unsupported field: j2z80.ZSystem." + fieldName,
        "j2z80.ZSystem", fieldName, fieldType.getSignature());
  }

  private static final class MemoryForget implements NeedsMemoryManager {
  }
}
