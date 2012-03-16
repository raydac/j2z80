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
package com.igormaznitsa.j2z80.ids;

import com.igormaznitsa.j2z80.aux.*;
import org.apache.bcel.generic.ClassGen;

public class ClassID {
    private final String className;
    
    public ClassID(final String className){
       Assert.assertNotNull("Class name must not be null", className);
       Assert.assertNotEmpty("Class name must not be empty", className);
       this.className = className;
    }
    
    public ClassID(final ClassGen classGen){
        Assert.assertNotNull("Argument must not be null", classGen);
        className = classGen.getClassName();
    }
    
    @Override
    public int hashCode(){
        return className.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof ClassID) {
            final ClassID meth = (ClassID) obj;
            return className.equals(meth.className);
        }
        return false;
    }
    
    public String getClassName(){
        return className;
    }
    
    public String makeClassLabel(){
        return LabelUtils.makeLabelNameForClass(className);
    }
    
    @Override
    public String toString(){
        return className;
    }
}
