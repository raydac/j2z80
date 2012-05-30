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
import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandSUB extends AbstractAsmCommand {

    public AsmCommandSUB() {
        super();
        addCase("B", (byte) 0x90);
        addCase("C", (byte) 0x91);
        addCase("D", (byte) 0x92);
        addCase("E", (byte) 0x93);
        addCase("H", (byte) 0x94);
        addCase("L", (byte) 0x95);
        addCase("(HL)", (byte) 0x96);
        addCase("A", (byte) 0x97);
    }

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
        final String arg = asm.getArgs()[0];
        
        if (doesNeedCalculation(arg)) {
            if (!isRegisterName(arg)) {
                int number;
                if (isIndexRegisterReference(arg)) {
                    number = new LightExpression(context,this, asm, extractCalculatedPart(arg)).calculate();
                    Assert.assertSignedByte(number);
                    return arg.startsWith("(IX") ? new byte[]{(byte) 0xDD, (byte) 0x96, (byte) number}:
                    new byte[]{(byte) 0xFD, (byte) 0x96, (byte) number};
                } else {
                    number = new LightExpression(context,this, asm, arg).calculate();
                    Assert.assertSignedByte(number);
                    return new byte[]{(byte) 0xD6, (byte) number};
                }
            }
        }
        return getPatternCase(asm.getSignature());
    }

    @Override
    public String getName() {
        return "SUB";
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ONE;
    }
}
