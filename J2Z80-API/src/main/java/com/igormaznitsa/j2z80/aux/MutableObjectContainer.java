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
package com.igormaznitsa.j2z80.aux;

/**
 * A Special object-wrapper allows to change an object and emulate changing of method arguments inside th method
 * 
 * @param <V> the type of the carried object
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MutableObjectContainer <V> {
    private V value;
    
    public MutableObjectContainer(){
        this(null);
    }
    
    public MutableObjectContainer(final V value){
        this.value = value;
    }
    
    public V get(){
        return this.value;
    }
    
    public void set(final V value) {
        this.value = value;
    }
    
    public boolean isNull(){
        return this.value == null;
    }
}
