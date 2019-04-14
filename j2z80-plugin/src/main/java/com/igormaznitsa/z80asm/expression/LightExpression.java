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
package com.igormaznitsa.z80asm.expression;

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

/**
 * It is a small easy expression parser. It allows to use '-' and '+' operators,
 * $ symbol as the current PC value, labels and strings
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class LightExpression {

  private final AsmTranslator context;
  private final String expression;
  private final AbstractAsmCommand callingCommand;
  private final ParsedAsmLine asmLineParameters;
  private int position;

  public LightExpression(final AsmTranslator context, final AbstractAsmCommand command, final ParsedAsmLine line, final String expression) {
    this.context = context;
    this.expression = expression;
    this.callingCommand = command;
    this.asmLineParameters = line;
  }

  private String nextToken() {
    if (position >= expression.length()) {
      return null;
    }
    final StringBuilder result = new StringBuilder();
    boolean atWorking = true;
    boolean insideString = false;
    boolean specialChar = false;
    while (atWorking && position < expression.length()) {
      final char chr = expression.charAt(position++);
      switch (chr) {
        case '-':
        case '+': {
          if (insideString) {
            result.append(chr);
          } else if (result.length() == 0) {
            result.append(chr);
            atWorking = false;
          } else {
            atWorking = false;
            position--;
          }
        }
        break;
        case '\"': {
          if (insideString) {
            result.append('\"');
            atWorking = false;
          } else {
            if (result.length() == 0) {
              result.append("\"");
              insideString = true;
              specialChar = false;
            } else {
              position--;
              atWorking = false;
            }
          }
        }
        break;
        case '$': {
          if (insideString) {
            result.append('$');
          } else {
            if (result.length() == 0) {
              result.append(context.getPC());
            } else {
              position--;
              atWorking = false;
            }
          }
        }
        break;
        case '\\': {
          if (insideString) {
            if (specialChar) {
              specialChar = false;
              result.append('\\');
            } else {
              specialChar = true;
            }
          }
        }
        break;
        default: {
          if (!insideString && Character.isWhitespace(chr)) {
            if (result.length() > 0) {
              atWorking = false;
            }
          } else {
            if (insideString && specialChar) {
              specialChar = false;
              switch (chr) {
                case 'n':
                  result.append('\n');
                  break;
                case 't':
                  result.append('\t');
                  break;
                case 'b':
                  result.append('\b');
                  break;
                case 'r':
                  result.append('\r');
                  break;
                case 'f':
                  result.append('\f');
                  break;
                case '"':
                  result.append('\"');
                  break;
                case '\'':
                  result.append('\'');
                  break;
                default:
                  throw new IllegalArgumentException("Unsupported special char detected [\\" + chr + ']');
              }
            } else {
              result.append(chr);
            }
          }
        }
      }
    }

    if (specialChar) {
      throw new IllegalArgumentException("Non-defined special char detected");
    }

    final String resultStr = result.toString();

    return resultStr.isEmpty() ? null : resultStr;
  }

  private int operandToNumber(final String str) {
    Assert.assertNotEmpty("Operand must not be an empty string", str);

    switch (str.charAt(0)) {
      case '\"': {
        // string
        Assert.assertTrue("String must be closed [" + str + ']', str.endsWith("\""));
        final String work = str.substring(1, str.length() - 1);
        Assert.assertTrue("String length must be greater than 2 chars [" + str + ']', str.length() > 2);
        int result = 0;
        for (final char chr : work.toCharArray()) {
          result = (result << 8) | (chr & 0xFF);
        }
        return result;
      }
      case '#': {
        // hex
        return Integer.parseInt(str.substring(1), 16);
      }
      case '%': {
        // binary
        return Integer.parseInt(str.substring(1), 2);
      }
      default: {
        // decimal number or label
        try {
          return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
          // label
          Integer address = context.findLabelAddress(str);
          if (address == null) {
            if (str.charAt(0) == '@') {
              // local label
              final int currentAddress = context.getPC();

              context.registerLocalLabelExpectant(str, (context, labelName, labelAddress) -> {
                final int currentPC = context.getPC();
                context.setPC(currentAddress);
                context.writeCode(callingCommand.makeMachineCode(context, asmLineParameters));
                context.setPC(currentPC);
              });
              address = context.getPC();
            } else {
              throw new IllegalArgumentException("Unknown label detected [" + str + ']', ex);
            }
          }
          return address;
        }
      }
    }
  }

  public int calculate() {
    position = 0;

    String lastOperation = null;
    Integer lastOperandStack = null;

    while (true) {
      final String token = nextToken();
      if (token == null) {
        break;
      }

      if ("+".equals(token) || "-".equals(token)) {
        // operation
        Assert.assertNull("Every operation must have operands [" + expression + ']', lastOperation);
        lastOperation = token;
      } else {
        // operand
        final int operand = operandToNumber(token);
        if (lastOperandStack == null) {
          if (lastOperation != null) {
            if ("-".equals(lastOperation)) {
              lastOperandStack = 0 - operand;
            } else {
              lastOperandStack = operand;
            }
            lastOperation = null;
          } else {
            lastOperandStack = operand;
          }
        } else {
          Assert.assertNotNull("There must be an operand in between of two operands [" + expression + ']', lastOperation);
          if ("+".equals(lastOperation)) {
            lastOperandStack = lastOperandStack + operand;
          } else if ("-".equals(lastOperation)) {
            lastOperandStack = lastOperandStack - operand;
          } else {
            throw new IllegalStateException("Unknown operation detected");
          }
          lastOperation = null;
        }
      }
    }

    Assert.assertNull("Every operation must have operands [" + expression + ']', lastOperation);
    Assert.assertNotNull("Expression must not be empty one", lastOperandStack);

    return lastOperandStack;
  }
}
