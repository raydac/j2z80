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
package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.utils.Assert;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The class implements a label data container.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class LabelAddressContainer {
    private boolean flagAllowReplace;
    private final Map<String,Integer> labelMap = new LinkedHashMap<String, Integer>();
    
    public LabelAddressContainer(){
        this(false);
    }
    
    public LabelAddressContainer(final boolean allowReplaceRecords){
        flagAllowReplace = allowReplaceRecords;
    }
    
    public boolean isReplaceAllowed(){
        return flagAllowReplace;
    }
    
    public void setReplaceAllowed(final boolean value){
        this.flagAllowReplace = value;
    }
    
    public boolean hasLabel(final String labelName){
        return labelMap.containsKey(labelName);
    }
    
    public int getLabelAddress(final String labelName){
        Assert.assertNotNull("Name must not be null", labelName);
        final Integer address = labelMap.get(labelName);
        Assert.assertNotNull("Only exist label must be requested", address);
    
        return address.intValue();
    }
    
    public void clear(){
        labelMap.clear();
    }
    
    public Set<Entry<String,Integer>> getSetOfRecords(){
        return labelMap.entrySet();
    }
    
    public boolean isEmpty(){
        return labelMap.isEmpty();
    }
    
    public void registerLabel(final String labelName, final int address){
        Assert.assertNotNull("Must not be null", labelName);
        Assert.assertAddress(address);
        
        final Integer addressAsInteger = Integer.valueOf(address);
        
        if (flagAllowReplace){
            labelMap.put(labelName, addressAsInteger);
        } else {
            Assert.assertFalse("Label must not be defined already ["+labelName+']', labelMap.containsKey(labelName));
            labelMap.put(labelName, addressAsInteger);
        }
    }
}
