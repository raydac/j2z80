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
package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandAND extends AbstractAsmCommand {

    public AsmCommandAND() {
        super();
        addCase("B", (byte) 0xA0);
        addCase("C", (byte) 0xA1);
        addCase("D", (byte) 0xA2);
        addCase("E", (byte) 0xA3);
        addCase("H", (byte) 0xA4);
        addCase("L", (byte) 0xA5);
        addCase("(HL)", (byte) 0xA6);
        addCase("A", (byte) 0xA7);
    }

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
        final String arg = asm.getArgs()[0];
        if (doesNeedCalculation(arg)) {
            if (!isRegisterName(arg)) {
                int number;
                if (isIndexRegisterReference(arg)) {
                    number = new LightExpression(context, this, asm, extractCalculatedPart(arg)).calculate();
                    Assert.assertUnsignedByte(number);
                    return arg.startsWith("(IX") ? new byte[]{(byte) 0xDD, (byte) 0xA6, (byte) number}
                            : new byte[]{(byte) 0xFD, (byte) 0xA6, (byte) number};
                } else {
                    number = new LightExpression(context, this, asm, arg).calculate();
                    Assert.assertUnsignedByte(number);
                    return new byte[]{(byte) 0xE6, (byte) number};
                }
            }
        }
        return getPatternCase(asm.getSignature());
    }

    @Override
    public String getName() {
        return "AND";
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ONE;
    }
}
