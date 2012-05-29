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
package com.igormaznitsa.j2z80;

/**
 * The interface describes a logger to be used inbound of the translator.
 * 
 * @author Igor Maznutsa (igor.maznitsa@igormaznitsa.com)
 */
public interface TranslatorLogger {
    /**
     * Print an information message
     * @param str an information message, it can be null
     */
    void logInfo(String str);
    
    /**
     * Print a warning message
     * @param str a warning message, it can be null
     */
    void logWarning(String str);
    
    /**
     * Print an error message
     * @param str an error message, it can be null
     */
    void logError(String str);
}
