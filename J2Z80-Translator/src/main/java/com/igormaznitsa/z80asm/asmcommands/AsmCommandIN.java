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

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandIN extends AbstractAsmCommand {

    public AsmCommandIN() {
        super();
        addCase("A,(C)", (byte) 0xED, (byte) 0x78);
        addCase("B,(C)", (byte) 0xED, (byte) 0x40);
        addCase("C,(C)", (byte) 0xED, (byte) 0x48);
        addCase("D,(C)", (byte) 0xED, (byte) 0x50);
        addCase("E,(C)", (byte) 0xED, (byte) 0x58);
        addCase("H,(C)", (byte) 0xED, (byte) 0x60);
        addCase("L,(C)", (byte) 0xED, (byte) 0x68);
    }

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
        final String port = asm.getArgs()[1];
        if ("(C)".equals(port)) {
            return getPatternCase(asm.getSignature());
        } else {
            final String leftPart = asm.getArgs()[0];
            Assert.assertTrue("The port must be in brakes [" + port + ']', isInBrakes(port));
            Assert.assertTrue("The left part must be A [" + leftPart + ']', "A".equals(leftPart));
            final int number = new LightExpression(context, this, asm, extractCalculatedPart(port)).calculate();
            Assert.assertUnsignedByte(number);
            return new byte[]{(byte) 0xDB, (byte) number};
        }
    }

    @Override
    public String getName() {
        return "IN";
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.TWO;
    }
}
