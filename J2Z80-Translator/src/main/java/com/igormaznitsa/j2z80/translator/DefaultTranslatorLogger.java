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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorLogger;

/**
 * The class implements a logger to be used by a translator and it prints
 * messages into System streams
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class DefaultTranslatorLogger implements TranslatorLogger {

    private static final String DEFAULT_LOG_PREFIX = "J2Z [%1$s] -> %2$s";

    @Override
    public void logInfo(final String message) {
        if (System.out != null) {
            System.out.println(String.format(DEFAULT_LOG_PREFIX, "INFO", message));
        }
    }

    @Override
    public void logWarning(final String message) {
        if (System.out != null) {
            System.out.println(String.format(DEFAULT_LOG_PREFIX, "WARN", message));
        }
    }

    @Override
    public void logError(final String message) {
        if (System.err != null) {
            System.err.println(String.format(DEFAULT_LOG_PREFIX, "ERR", message));
        }
    }
}
