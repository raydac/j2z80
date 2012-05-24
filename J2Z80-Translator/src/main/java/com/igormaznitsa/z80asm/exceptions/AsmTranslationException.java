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
package com.igormaznitsa.z80asm.exceptions;

/**
 * It is the special exception which will be thrown if there is some problems in code translation.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@SuppressWarnings("serial")
public class AsmTranslationException extends RuntimeException {
    private final String srcString;
    private final int lineNumber;
    
    public AsmTranslationException(final String message, final String srcString, final int lineNumber, final Throwable cause){
        super(message,cause);
        this.srcString = srcString;
        this.lineNumber = lineNumber;
    }
    
    public String getSrcString(){
        return srcString;
    }
    
    public int getLineNumber(){
        return lineNumber;
    }
    
    @Override
    public String toString(){
        return getMessage()+"\'"+getSrcString()+"\' at "+getLineNumber()+" line";
    }
}
