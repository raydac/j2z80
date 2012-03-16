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

public class AsmCommandDEFW extends AbstractAsmCommand {

    @Override
    public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
        final byte[] result = new byte[asm.getArgs().length << 1];
        int index = 0;
        for (final String arg : asm.getArgs()) {
            final int value = new LightExpression(context, this, asm, arg).calculate();
            if (value < 0) {
                Assert.assertSignedShort(value);
            } else {
                Assert.assertUnsignedShort(value);
            }

            result[index++] = (byte) value;
            result[index++] = (byte) (value >>> 8);
        }
        return result;
    }

    @Override
    public String getName() {
        return "DEFW";
    }

    @Override
    public Arguments getAllowedArgumentsNumber() {
        return Arguments.ONE_OR_MORE;
    }

    @Override
    public boolean isSpecialDirective() {
        return true;
    }
}
