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

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.LabelAndFrameUtils;
import org.apache.bcel.generic.ClassGen;

/**
 * The class describes CLASS ID which is being used by the translator to identify a java class during processing.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ClassID {
    // inside storage of the full class name
    private final String className;
    
    /**
     * The Constructor creates the new instance based on the full class path name
     * @param className the full canonical class path name, must not be null
     */
    public ClassID(final String className){
       Assert.assertNotNull("Class name must not be null", className);
       Assert.assertNotEmpty("Class name must not be empty", className);
       this.className = className;
    }
    
    /**
     * The Constructor create the new instance based on a ClassGet object
     * @param classGen the object to be used for creation, must not be null
     */
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
    
    /**
     * Get the full class name
     * @return the full class name as String
     */
    public String getClassName(){
        return className;
    }
    
    /**
     * Make the label for the class
     * @return a String contains the label for the class name
     */
    public String makeClassLabel(){
        return LabelAndFrameUtils.makeLabelNameForClass(className);
    }
    
    @Override
    public String toString(){
        return className;
    }
}
