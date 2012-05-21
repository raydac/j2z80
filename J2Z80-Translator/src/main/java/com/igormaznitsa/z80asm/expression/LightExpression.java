/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 * 
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.igormaznitsa.z80asm.expression;

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.LocalLabelExpectant;
import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

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
                default: {
                    if (Character.isWhitespace(chr)) {
                        if (result.length() > 0) {
                            atWorking = false;
                        } else {
                            continue;
                        }
                    } else {
                        result.append(chr);
                    }
                }
            }
        }
        return result.toString();
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

                            context.registerLocalLabelExpectant(str, new LocalLabelExpectant() {

                                @Override
                                public void onLabelIsAccessible(final AsmTranslator context, final String labelName, final long labelAddress) {
                                    final int currentPC = context.getPC();
                                    context.setPC(currentAddress);
                                    context.writeCode(callingCommand.makeMachineCode(context, asmLineParameters));
                                    context.setPC(currentPC);
                                }
                            });
                            address = Integer.valueOf(context.getPC());
                        } else {
                            throw new IllegalArgumentException("Unknown label detected [" + str + ']',ex);
                        }
                    }
                    return address.intValue();
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
                            lastOperandStack = Integer.valueOf(0 - operand);
                        } else {
                            lastOperandStack = Integer.valueOf(operand);
                        }
                        lastOperation = null;
                    } else {
                        lastOperandStack = Integer.valueOf(operand);
                    }
                } else {
                    Assert.assertNotNull("There must be an operand in between of two operands [" + expression + ']', lastOperation);
                    if ("+".equals(lastOperation)) {
                        lastOperandStack = Integer.valueOf(lastOperandStack.intValue() + operand);
                    } else if ("-".equals(lastOperation)) {
                        lastOperandStack = Integer.valueOf(lastOperandStack.intValue() - operand);
                    } else {
                        throw new IllegalStateException("Unknown operation detected");
                    }
                    lastOperation = null;
                }
            }
        }

        Assert.assertNull("Every operation must have operands [" + expression + ']', lastOperation);
        Assert.assertNotNull("Expression must not be empty one", lastOperandStack);

        return lastOperandStack.intValue();
    }
}
