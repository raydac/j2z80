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
package com.igormaznitsa.impl;

public class MemoryFillerFactory {
    public static final int FILLER_JAVA = 12364;
    public static final int FILLER_NATIVE = 2212;
    
    private static final Object [] fillers = new Object[]{(Object)new MemoryFillerJava(), (Object)new MemoryFillerNative()};
    
    static {
        for(int i=0;i<fillers.length;i++){
            if (!(fillers[i] instanceof MemoryFiller)){
               fillers[i] = null;
            }
        }
    }
    
    
    public static MemoryFiller getFiller(final int type){
        switch(type){
            case FILLER_JAVA : return (MemoryFiller)fillers[0];
            case FILLER_NATIVE : return (MemoryFiller)fillers[1];
            default: return null;
        }
    }
}
