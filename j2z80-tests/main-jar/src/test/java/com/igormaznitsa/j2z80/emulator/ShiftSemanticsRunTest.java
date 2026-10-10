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

package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import com.igormaznitsa.j2z80.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ShiftSemanticsRunTest {

  private static final List<Integer> INT_VALUES =
      List.of(0, 1, -1, (int) Short.MIN_VALUE, (int) Short.MAX_VALUE, -32767, 0x4000,
          -0x4000, 0x5555, -0x5555, 0x1234, -0x1234);
  private static final List<Integer> INT_COUNTS = buildCounts(32,
      List.of(-33, -32, -1, 31, 32, 33, 63, 64, Integer.MIN_VALUE, Integer.MAX_VALUE));
  private static final List<Integer> LONG_VALUES =
      List.of(0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0x40000000, -0x40000000,
          0x55555555, 0xAAAAAAAA, 0x12345678, -0x12345678);
  private static final List<Integer> LONG_COUNTS = buildCounts(64,
      List.of(-65, -64, -63, -33, -32, -1, 63, 64, 65, 127, 128,
          Integer.MIN_VALUE, Integer.MAX_VALUE));

  private static List<Integer> buildCounts(final int allEffectiveCounts,
                                           final List<Integer> extraCounts) {
    final List<Integer> result = new ArrayList<>();
    for (int count = 0; count < allEffectiveCounts; count++) {
      result.add(count);
    }
    result.addAll(extraCounts);
    return List.copyOf(result);
  }

  @Test
  public void intShiftsMatchSixteenBitRuntimeAcrossAllMaskedCounts() throws Exception {
    for (final String opcode : List.of("ISHL", "ISHR", "IUSHR")) {
      this.assertOpcodeCases(opcode, INT_VALUES, INT_COUNTS);
    }
  }

  @Test
  public void longShiftsMatchThirtyTwoBitRuntimeAcrossAllMaskedCounts() throws Exception {
    for (final String opcode : List.of("LSHL", "LSHR", "LUSHR")) {
      this.assertOpcodeCases(opcode, LONG_VALUES, LONG_COUNTS);
    }
  }

  private void assertOpcodeCases(final String opcode, final List<Integer> values,
                                 final List<Integer> counts) throws Exception {
    final List<ShiftCase> cases = new ArrayList<>();
    for (final int value : values) {
      for (final int count : counts) {
        cases.add(new ShiftCase(opcode, value, count));
      }
    }

    final Program program = Program.assemble(this.assembleCases(opcode, cases));
    final Z80Machine machine = new Z80Machine();
    machine.load(program);
    machine.runUntilHalt(program.origin(), 2_000_000);

    for (int index = 0; index < cases.size(); index++) {
      final ShiftCase shiftCase = cases.get(index);
      if (opcode.startsWith("I")) {
        assertEquals(shiftCase.toString(), this.expectedIntResult(shiftCase),
            machine.wordAt(program.addressOf("SHIFT_RESULT_" + index)));
      } else {
        final int actual = machine.wordAt(program.addressOf("SHIFT_RESULT_" + index))
            | machine.wordAt(program.addressOf("SHIFT_RESULT_" + index + "_HIGH")) << 16;
        assertEquals(shiftCase.toString(), this.expectedLongResult(shiftCase), actual);
      }
    }
  }

  private String assembleCases(final String opcode, final List<ShiftCase> cases) throws Exception {
    final StringBuilder source = new StringBuilder("ORG #6000\nLD SP,#F000\n");
    for (int index = 0; index < cases.size(); index++) {
      final ShiftCase shiftCase = cases.get(index);
      source.append("SHIFT_CASE_").append(index).append(":\n");
      if (shiftCase.opcode().startsWith("I")) {
        source.append("LD BC,").append(shiftCase.value() & 0xFFFF).append("\nPUSH BC\n")
            .append("LD BC,").append(shiftCase.count() & 0xFFFF).append("\nPUSH BC\n")
            .append(Utils.readTextResource(AbstractJvmCommandProcessor.class,
                shiftCase.opcode() + ".a80"))
            .append("POP HL\nLD (SHIFT_RESULT_").append(index).append("),HL\n");
      } else {
        source.append("LD BC,").append(shiftCase.value() >>> 16 & 0xFFFF).append("\nPUSH BC\n")
            .append("LD BC,").append(shiftCase.value() & 0xFFFF).append("\nPUSH BC\n")
            .append("LD BC,").append(shiftCase.count() & 0xFFFF).append("\nPUSH BC\n")
            .append(Utils.readTextResource(AbstractJvmCommandProcessor.class,
                shiftCase.opcode() + ".a80"))
            .append("POP HL\nLD (SHIFT_RESULT_").append(index).append("),HL\n")
            .append("POP HL\nLD (SHIFT_RESULT_").append(index).append("_HIGH),HL\n");
      }
    }
    source.append("HALT\n");
    for (int index = 0; index < cases.size(); index++) {
      source.append("SHIFT_RESULT_").append(index).append(": DEFW 0\n");
      if (cases.get(index).opcode().startsWith("L")) {
        source.append("SHIFT_RESULT_").append(index).append("_HIGH: DEFW 0\n");
      }
    }
    if (opcode.startsWith("L")) {
      source.append(Utils.readTextResource(AbstractJvmCommandProcessor.class,
              "LONG_ARITHMETIC_MANAGER.a80"))
          .append("___ATHROW_PROCESSING_CODE_ADDRESS: DEFW 0\n");
    }
    return source.toString();
  }

  private int expectedIntResult(final ShiftCase shiftCase) {
    final int count = shiftCase.count() & 31;
    final int value = (short) shiftCase.value();
    return switch (shiftCase.opcode()) {
      case "ISHL" -> value << count & 0xFFFF;
      case "ISHR" -> value >> count & 0xFFFF;
      case "IUSHR" -> count >= 16 ? 0 : (shiftCase.value() & 0xFFFF) >>> count;
      default -> throw new IllegalArgumentException("Unexpected opcode " + shiftCase.opcode());
    };
  }

  private int expectedLongResult(final ShiftCase shiftCase) {
    final int count = shiftCase.count() & 63;
    final int value = shiftCase.value();
    return switch (shiftCase.opcode()) {
      case "LSHL" -> count >= 32 ? 0 : value << count;
      case "LSHR" -> count >= 32 ? (value < 0 ? -1 : 0) : value >> count;
      case "LUSHR" -> count >= 32 ? 0 : value >>> count;
      default -> throw new IllegalArgumentException("Unexpected opcode " + shiftCase.opcode());
    };
  }

  private record ShiftCase(String opcode, int value, int count) {
  }
}
