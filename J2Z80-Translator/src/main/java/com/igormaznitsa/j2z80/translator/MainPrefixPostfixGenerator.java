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

import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;

/**
 * The class generates both the prefix and the postfix code for the main method
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MainPrefixPostfixGenerator {
    private final ClassMethodInfo method;
    private final int initStack;
    private final int startAddress;
    
    /**
     * The Constructor.
     * 
     * @param mainMethod the main method to be used in operations, must not be null
     * @param startAddress the start address of the main method 
     * @param stackInitAddr the value to init stack just after start
     */
    public MainPrefixPostfixGenerator(final ClassMethodInfo mainMethod, final int startAddress, final int stackInitAddr) {
        this.method = mainMethod;
        this.initStack = stackInitAddr;
        this.startAddress = startAddress;
    }
    
    /**
     * Generate assembler prefix for the main method.
     * @return array of assembler lines to be used as the prefix for the main method
     */
    public String [] generatePrefix() {
        final StringBuilder result = new StringBuilder();
        
        final int maxLocalsForMainMethod = method.getMethodGen().getMaxLocals();
        final int frameStack = (maxLocalsForMainMethod+1)<<1;
        
        result.append("ORG ").append(startAddress).append('\n');
        result.append("DI").append('\n');
        result.append("LD IX,").append(initStack).append('\n');
        result.append("LD SP,").append(initStack-frameStack).append('\n');
        result.append("LD BC,___MAINLOOP___\n").append("PUSH BC\n");
        
        return Utils.breakToLines(result.toString());
    }
    
    /**
     * Generate the postfix for the main method.
     * @return array of assembler lines to be used as the postfix for the main method
     */
    public String [] generatePostfix() {
        return Utils.breakToLines("___MAINLOOP___: JP ___MAINLOOP___");
    }
    
}
