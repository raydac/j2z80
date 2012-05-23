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

/**
 * An Auxiliary class to check arguments.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public enum Assert {
    ;

    /**
     * Check that an object is not null.
     * @param msg the message to notify that the object must not be null
     * @param obj the object to be checked
     */
    public static void assertNotNull(final String msg, final Object obj) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }
    }

    /**
     * Check that there is not the null object in an object array
     * @param msg the message to notify that the objects must not contain null 
     * @param objects the object array to be checked 
     */
    public static void assertNotNull(final String msg, final Object... objects) {
        if (objects == null) {
            throw new NullPointerException("Array of objects is null");
        }
        for (final Object o : objects) {
            if (o == null) {
                throw new NullPointerException(msg);
            }
        }
    }

    /**
     * Check that an integer value in signed byte bounds
     * @param value an integer value to be checked 
     */
    public static void assertSignedByte(final int value) {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Signed byte must be in " + Byte.MIN_VALUE + ".." + Byte.MAX_VALUE + " [" + value + ']');
        }
    }

    /**
     * Check that an integer in unsigned byte bounds
     * @param value an integer value to be checked
     */
    public static void assertUnsignedByte(final int value) {
        if ((value & 0xFFFFFF00) != 0) {
            throw new IllegalArgumentException("Unsigned byte must be in 8 bit bounds [" + value + ']');
        }
    }

    /**
     * Check that an integer in signed short bounds
     * @param value an integer to be checked
     */
    public static void assertSignedShort(final int value) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Signed short must be in " + Short.MIN_VALUE + ".." + Short.MAX_VALUE + " [" + value + ']');
        }
    }

    /**
     * Check that an integer in unsigned short bounds
     * @param value an integer to be checked
     */
    public static void assertUnsignedShort(final int value) {
        if ((value & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException("Unsigned short must be in 16 bit bounds [" + value + ']');
        }
    }

    /**
     * Check that an integer can represent 16 bit address
     * @param value an integer value to be checked
     */
    public static void assertAddress(final int value) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Address must be in the 0x0...0xFFFF interval [0x" + Integer.toHexString(value).toUpperCase() + ']');
        }
    }

    /**
     * Check that an integer has zero value
     * @param msg the message to declare that the integer must be zero
     * @param value an integer value to be checked
     */
    public static void assertZero(final String msg, final int value) {
        if (value != 0) {
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Check that a byte array is not null and not empty one
     * @param msg the information message
     * @param array a byte array to be checked
     */
    public static void assertNotEmpty(final String msg, byte[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check that a String is not null and not empty one
     * @param msg the information message
     * @param str a String to be checked
     */
    public static void assertNotEmpty(final String msg, String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check that a collection is not null and not empty one
     * @param msg the information message
     * @param col a collection to be checked 
     */
    public static void assertNotEmpty(final String msg, final Collection<?> col) {
        if (col.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check that a map is not null and not empty one
     * @param msg the information message
     * @param map a map to be checked 
     */
    public static void assertEmpty(final String msg, final Map<?, ?> map) {
        if (!map.isEmpty()) {
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Check that a collection is not null and empty one
     * @param msg the information message
     * @param collection a collection to be checked
     */
    public static void assertEmpty(final String msg, final Collection<?> collection) {
        if (!collection.isEmpty()) {
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Check validity of a label name
     * @param labelName a label name to be checked
     */
    private static void checkLabelNameChars(final String labelName) {
        if (labelName.isEmpty()) {
            throw new IllegalArgumentException("Label must not be an empty string");
        }
        for (final char c : labelName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Label name must not contain any kind of a white character [" + labelName + ']');
            }
        }
    }

    /**
     * Check validity of a local label name
     * @param labelName a label name to be checked
     */
    public static void assertLocalLabelName(final String labelName) {
        checkLabelNameChars(labelName);
        if (!labelName.startsWith("@")) {
            throw new IllegalArgumentException("Must be a local label name, it starts with the @ symbol [" + labelName + ']');
        }
    }

    /**
     * Check validity of a global label name
     * @param labelName a label name to be checked
     */
    public static void assertGlobalLabelName(final String labelName) {
        checkLabelNameChars(labelName);
        if (labelName.startsWith("@")) {
            throw new IllegalArgumentException("Must be a global label name, it must not start with the @ symbol [" + labelName + ']');
        }
    }

    /**
     * Check that an object is null
     * @param msg the information message
     * @param obj an object to be checked
     */
    public static void assertNull(final String msg, final Object obj) {
        if (obj != null) {
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Check that an integer value is not zero
     * @param msg the information message
     * @param value an ineteger value to be checked
     */
    public static void assertNonZero(final String msg, final int value) {
        if (value == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check that a boolean value is false
     * @param msg the information message
     * @param flag a flag to be checked
     */
    public static void assertFalse(final String msg, final boolean flag) {
        if (flag) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check that a boolean value is true
     * @param msg the information message
     * @param flag a flag to be checked
     */
    public static void assertTrue(final String msg, final boolean flag) {
        if (!flag) {
            throw new IllegalArgumentException(msg);
        }
    }
}
