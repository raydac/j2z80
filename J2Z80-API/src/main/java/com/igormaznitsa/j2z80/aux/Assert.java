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

import java.util.Collection;
import java.util.Map;

public class Assert {

    public static void assertNotNull(final String msg, final Object obj) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }
    }

    public static void assertNotNull(final String msg, final Object... obj) {
        if (obj == null)
            throw new NullPointerException("Array of objects is null");
        for (final Object o : obj) {
            if (o == null) {
                throw new NullPointerException(msg);
            }
        }
    }

    public static void assertSignedByte(final int arg){
        if (arg<Byte.MIN_VALUE || arg>Byte.MAX_VALUE)
            throw new IllegalArgumentException("Signed byte must be in "+Byte.MIN_VALUE+".."+Byte.MAX_VALUE+" ["+arg+']');
    }
    
    public static void assertUnsignedByte(final int arg){
        if ((arg & 0xFFFFFF00)!=0)
            throw new IllegalArgumentException("Unsigned byte must be in 8 bit bounds ["+arg+']');
    }
    
    public static void assertSignedShort(final int arg){
        if (arg<Short.MIN_VALUE || arg>Short.MAX_VALUE)
            throw new IllegalArgumentException("Signed short must be in "+Short.MIN_VALUE+".."+Short.MAX_VALUE+" ["+arg+']');
    }
    
    public static void assertUnsignedShort(final int arg){
        if ((arg & 0xFFFF0000)!=0)
            throw new IllegalArgumentException("Unsigned short must be in 16 bit bounds ["+arg+']');
    }
    
    public static void assertAddress(final int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Address must be in the 0x0...0xFFFF interval [0x"+Integer.toHexString(value).toUpperCase()+']');
        }
    }

    public static void assertZero(final String msg, final int size) {
        if (size != 0) {
            throw new IllegalStateException(msg);
        }
    }

    public static void assertNotEmpty(final String msg, byte [] array) {
        if (array.length == 0){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertNotEmpty(final String msg, String str) {
        if (str.isEmpty()){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertNotEmpty(final String msg, final Collection<?> set) {
        if (set.isEmpty()){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertEmpty(final String msg, final Map<?,?> collection) {
        if (!collection.isEmpty()){
            throw new IllegalStateException(msg);
        }
    }

    public static void assertEmpty(final String msg, final Collection<?> collection) {
        if (!collection.isEmpty()){
            throw new IllegalStateException(msg);
        }
    }

    private static void checkLabelNameChars(final String labelName){
        if (labelName.isEmpty()){
            throw new IllegalArgumentException("Label must not be an empty string");
        }
        for (final char c : labelName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Label name must not contain any kind of a white character [" + labelName + ']');
            }
        }
    }
    
    public static void assertLocalLabelName(final String labelName) {
        checkLabelNameChars(labelName);
        if (!labelName.startsWith("@")){
            throw new IllegalArgumentException("Must be a local label name, it starts with the @ symbol ["+labelName+']');
        }
    }

    public static void assertGlobalLabelName(final String labelName) {
        checkLabelNameChars(labelName);
        if (labelName.startsWith("@")) {
            throw new IllegalArgumentException("Must be a global label name, it must not start with the @ symbol [" + labelName + ']');
        }
    }

    public static void assertNull(final String msg, final Object obj) {
        if (obj != null){
            throw new IllegalStateException(msg);
        }
    }

    public static void assertNonZero(final String msg, final int value) {
        if (value == 0){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertFalse(final String msg, final boolean flag) {
        if (flag){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertTrue(final String msg, final boolean flag) {
        if (!flag){
            throw new IllegalArgumentException(msg);
        }
    }

}
