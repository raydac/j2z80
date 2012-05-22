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
package com.igormaznitsa.j2z80.translator.optimizator;

import java.util.Locale;

public enum OptimizationLevel {

    NONE("none"),
    BASE("base");
    private final String textName;

    private OptimizationLevel(final String textName) {
        this.textName = textName;
    }

    public String getTextName() {
        return this.textName;
    }

    public static OptimizationLevel findForTextName(final String textName) {
        final String textNameInLowCase = textName.toLowerCase(Locale.ENGLISH);
        for (final OptimizationLevel value : values()) {
            if (value.getTextName().toLowerCase(Locale.ENGLISH).equals(textNameInLowCase)) {
                return value;
            }
        }
        return NONE;
    }
}