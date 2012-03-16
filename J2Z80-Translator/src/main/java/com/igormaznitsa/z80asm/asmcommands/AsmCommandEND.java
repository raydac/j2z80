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
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandEND extends AbstractAsmCommand {

    public static final String NAME = "END";
    
    @Override
    public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
        final StringBuilder info = new StringBuilder(NAME);
        info.append(" -> ");
        
        for(final String arg : asm.getArgs()){
            if (isString(arg)){
                info.append(arg).append(' ');
            } else {
                final long value = new LightExpression(context, this, asm, arg).calculate();
                info.append(value).append(' ');
            }
        }
        if (asm.getArgs().length>0){
            context.printText(info.toString());
        }
        return EMPTY_ARRAY;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ANY;
    }
    
    @Override
    public boolean isSpecialDirective() {
        return true;
    }
}
