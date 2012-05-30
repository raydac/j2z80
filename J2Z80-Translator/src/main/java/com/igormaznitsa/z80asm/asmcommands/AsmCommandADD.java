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

public class AsmCommandADD extends AbstractAsmCommand {
    public AsmCommandADD(){
        super();
        addCase("A,A", (byte)0x87);
        addCase("A,B", (byte)0x80);
        addCase("A,C", (byte)0x81);
        addCase("A,D", (byte)0x82);
        addCase("A,E", (byte)0x83);
        addCase("A,H", (byte)0x84);
        addCase("A,L", (byte)0x85);
        addCase("A,(HL)", (byte)0x86);
        addCase("HL,BC", (byte)0x09);
        addCase("HL,DE", (byte)0x19);
        addCase("HL,HL", (byte)0x29);
        addCase("HL,SP", (byte)0x39);
        addCase("IX,BC", (byte)0xDD,(byte)0x09);
        addCase("IX,DE", (byte)0xDD,(byte)0x19);
        addCase("IX,IX", (byte)0xDD,(byte)0x29);
        addCase("IX,SP", (byte)0xDD,(byte)0x39);
        addCase("IY,BC", (byte)0xFD,(byte)0x09);
        addCase("IY,DE", (byte)0xFD,(byte)0x19);
        addCase("IY,IY", (byte)0xFD,(byte)0x29);
        addCase("IY,SP", (byte)0xFD,(byte)0x39);
    }

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, ParsedAsmLine asm) {
        final String arg0 = asm.getArgs()[0];
        final String arg1 = asm.getArgs()[1];
        
        if ("A".equals(arg0)){
            if (doesNeedCalculation(arg1)){
                int number;
                if (!isRegisterName(arg1)){
                    if (isIndexRegisterReference(arg1)){
                        number = new LightExpression(context, this, asm, extractCalculatedPart(arg1)).calculate();
                        Assert.assertUnsignedByte(number);
                        return arg1.startsWith("(IX")? new byte[]{(byte)0xDD, (byte)0x86,(byte)number}:
                        new byte[]{(byte)0xFD, (byte)0x86,(byte)number};
                    } else {
                        number = new LightExpression(context,this, asm, arg1).calculate();
                        Assert.assertUnsignedByte(number);
                        return new byte[]{(byte)0xC6,(byte)number};
                    }
                }
            }
        }
        return getPatternCase(asm.getSignature());
    }

    @Override
    public String getName() {
        return "ADD";
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.TWO;
    }
    
}
