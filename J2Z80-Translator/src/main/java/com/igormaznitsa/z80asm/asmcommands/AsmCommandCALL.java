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

public class AsmCommandCALL extends AbstractAsmCommand {

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
        if (asm.getArgs().length == 1) {
            final int address = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
            Assert.assertAddress(address);
            return new byte[]{(byte) 0xCD, (byte) address, (byte) (address >>> 8)};
        } else {
            final String flag = asm.getArgs()[0];
            final int address = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
            Assert.assertAddress(address);
            byte command = 0;

            if ("NZ".equals(flag)) {
                command = (byte) 0xC4;
            } else if ("Z".equals(flag)) {
                command = (byte) 0xCC;
            } else if ("NC".equals(flag)) {
                command = (byte) 0xD4;

            } else if ("C".equals(flag)) {
                command = (byte) 0xDC;

            } else if ("PO".equals(flag)) {
                command = (byte) 0xE4;

            } else if ("PE".equals(flag)) {
                command = (byte) 0xEC;

            } else if ("P".equals(flag)) {
                command = (byte) 0xF4;

            } else if ("M".equals(flag)) {
                command = (byte) 0xFC;

            } 
            
            Assert.assertNonZero("Unsupported flag for CALL command [" + flag + ']', command);

            return new byte[]{command, (byte) address, (byte) (address >>> 8)};
        }
    }

    @Override
    public String getName() {
        return "CALL";
    }
    
    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ONE_OR_TWO;
    }
}
