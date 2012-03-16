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

public class AsmCommandEX extends AbstractAsmCommand {

    public AsmCommandEX(){
        super();
        addCase("DE,HL", (byte)0xEB);
        addCase("(SP),HL",(byte)0xE3);
        addCase("(SP),IX", (byte) 0xDD,(byte)0xE3);
        addCase("(SP),IY", (byte) 0xFD,(byte)0xE3);
        addCase("AF,AF'",(byte)0x08);
    }
    
    @Override
    public byte[] makeMachineCode(AsmTranslator context, final ParsedAsmLine asm) {
        return getPatternCase(asm.getSignature()); 
    }

    @Override
    public String getName() {
        return "EX";
    }
    
    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.TWO;
    }
    
}
