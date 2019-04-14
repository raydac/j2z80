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
package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertTrue;

public class AllOperationTest {

  private static final AsmTranslator MOCK_CONTEXT = Mockito.mock(AsmTranslator.class);
  private static final String VALUE_N = "7D";
  private static final String VALUE_NN = "1AFE";
  private static final String VALUE_E = "12";
  private static final String VALUE_D = "03";

  private static String normalizeCodeBlock(final String codeBlock) {
    final String[] splitted = codeBlock.split(" ");
    int counterN = 0;
    for (final String str : splitted) {
      if ("n".equals(str)) {
        counterN++;
      }
    }

    if (counterN > 2) {
      throw new IllegalArgumentException("Macros N has been met more than two times");
    }

    final StringBuilder result = new StringBuilder();

    int positionAtNN = 1;

    for (final String str : splitted) {
      if (str.length() > 2) {
        for (final String pair : splitToPairs(str)) {
          if (result.length() > 0) {
            result.append(' ');
          }
          result.append(pair);
        }
      } else if ("d".equals(str)) {
        if (result.length() > 0) {
          result.append(' ');
        }
        result.append(VALUE_D);
      } else if ("e".equals(str)) {
        if (result.length() > 0) {
          result.append(' ');
        }
        result.append(VALUE_E);
      } else if ("n".equals(str)) {
        if (result.length() > 0) {
          result.append(' ');
        }
        if (counterN == 1) {
          result.append(VALUE_N);
        } else {
          if (positionAtNN == 1) {
            result.append(VALUE_NN.substring(2));
            positionAtNN--;
          } else {
            result.append(VALUE_NN.substring(0, 2));
          }
        }
      } else {
        if (result.length() > 0) {
          result.append(' ');
        }
        if (str.length() < 2) {
          result.append('0');
        }
        result.append(str);
      }
    }
    return result.toString();
  }

  private static String[] splitToPairs(final String str) {
    if ((str.length() & 1) == 1) {
      throw new IllegalArgumentException("Odd string length");
    }
    final String[] result = new String[str.length() / 2];

    int strIndex = 0;

    for (int index = 0; index < str.length(); index += 2) {
      final String pair = new StringBuilder().append(str.charAt(index)).append(str.charAt(index + 1)).toString();
      result[strIndex++] = pair.toUpperCase(Locale.ENGLISH);
    }

    return result;
  }

  @Test
  public void testAllCommands() throws Exception {
    final BufferedReader testInfoFile = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("all_commands.txt")));
    final List<String> testDataBuffer = new ArrayList<String>(1400);
    try {
      while (true) {
        final String line = testInfoFile.readLine();
        if (line == null) {
          break;
        }
        if (line.trim().isEmpty()) {
          continue;
        }
        testDataBuffer.add(line.trim());
      }
      System.out.println("Read " + testDataBuffer.size() + " lines of test data");
    } finally {
      testInfoFile.close();
    }

    for (final String testLine : testDataBuffer) {
      executeTest(testLine);
    }
  }

  private String removeQuotes(final String str) {
    final String result = str.substring(1, str.length() - 1);
    return result.trim();
  }

  private void executeTest(final String testData) {
    final String[] splitted = testData.split("\\;");
    final String codeBlock = removeQuotes(splitted[0]).trim();
    final String asmBlock = removeQuotes(splitted[1]).trim();

    if (asmBlock.endsWith("*")) {
      // it's a undocumented command
      System.out.println("Skipping undocumented " + asmBlock);
      return;
    } else {
      System.out.print("Test for " + asmBlock + "...............");
      final byte[] generated = generateMachineCode(normalizeAsmBlock(asmBlock));
      assertTrue("Wrong machine code for " + asmBlock + " " + arrayAsString(generated), checkMachineCode(normalizeCodeBlock(codeBlock), generated));
      System.out.println("PASSED");
    }
  }

  private String arrayAsString(final byte[] array) {
    final StringBuilder result = new StringBuilder();

    result.append('[');

    for (byte value : array) {
      result.append(' ').append(Integer.toHexString(value & 0xFF));
    }
    result.append(" ]");
    return result.toString();
  }

  private boolean checkMachineCode(final String etalon, final byte[] generated) {
    final String[] splittedCodes = etalon.split(" ");
    if (splittedCodes.length != generated.length) {
      return false;
    }

    int index = 0;
    for (final String str : splittedCodes) {
      final int value = Integer.parseInt(str, 16);
      if (value != (generated[index++] & 0xFF)) {
        return false;
      }
    }
    return true;
  }

  private byte[] generateMachineCode(final String asmCommand) {
    final ParsedAsmLine asm = new ParsedAsmLine(asmCommand);
    final AbstractAsmCommand command = AbstractAsmCommand.findCommandForName(asm.getCommand());
    if (command == null) {
      throw new IllegalArgumentException("Unsupported assembler command detected [" + asm.getCommand() + ']');
    }

    if (!command.getAllowedArgumentsNumber().check(asm.getArgs())) {
      throw new IllegalArgumentException("Unsupported argument number detected [" + asmCommand + ']');
    }
    return command.makeMachineCode(MOCK_CONTEXT, asm);
  }

  private String normalizeAsmBlock(final String asmBlock) {
    return asmBlock.replace("d", "#" + VALUE_D).replace("nn", "#" + VALUE_NN).replace("n", "#" + VALUE_N).replace("e", "#" + VALUE_E);
  }
}
