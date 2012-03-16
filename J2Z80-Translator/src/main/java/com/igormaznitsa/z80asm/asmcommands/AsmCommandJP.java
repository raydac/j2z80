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

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandJP extends AbstractAsmCommand {

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
         if (asm.getArgs().length == 1) {
            if (isInBrakes(asm.getArgs()[0])){
                final String arg = asm.getArgs()[0];
                
                byte [] result = null;
                
                if ("(HL)".equals(arg)){
                    result = new byte[]{(byte)0xE9};
                } else if ("(IX)".equals(arg)){
                    result = new byte[]{(byte)0xDD,(byte)0xE9};
                } else if ("(IY)".equals(arg)){
                    result = new byte[]{(byte)0xFD,(byte)0xE9};
                } 
                
                Assert.assertNotNull("Wrong register usage for JP command [" + arg + ']', result);

                return result;
            } else {
            final int address = new LightExpression(context, this, asm, asm.getArgs()[0]).calculate();
            Assert.assertAddress(address);
            return new byte[]{(byte) 0xC3, (byte) address, (byte) (address >>> 8)};
            }
         } else {
            final String flag = asm.getArgs()[0];
            final int address = new LightExpression(context, this, asm, asm.getArgs()[1]).calculate();
            Assert.assertAddress(address);
            byte command = 0;

            if ("NZ".equals(flag)) {
                command = (byte) 0xC2;
            } else if ("Z".equals(flag)) {
                command = (byte) 0xCA;
            } else if ("NC".equals(flag)) {
                command = (byte) 0xD2;
            } else if ("C".equals(flag)) {
                command = (byte) 0xDA;
            } else if ("PO".equals(flag)) {
                command = (byte) 0xE2;
            } else if ("PE".equals(flag)) {
                command = (byte) 0xEA;
            } else if ("P".equals(flag)) {
                command = (byte) 0xF2;
            } else if ("M".equals(flag)) {
                command = (byte) 0xFA;
            }
            
            Assert.assertNonZero("Unsupported flag for JP command [" + flag + ']', command);

            return new byte[]{command, (byte) address, (byte) (address >>> 8)};
        }
    }

    @Override
    public String getName() {
        return "JP";
    }
    
     @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ONE_OR_TWO;
    }
}
