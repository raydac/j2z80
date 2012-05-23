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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.util.AntPathMatcher;

public enum Utils {

    ;
    public static final String NEXT_LINE = "\r\n";

    /**
     * Silently close any closeable object
     *
     * @param closeableOne the object to be closed, it can be null
     */
    public static void silentlyClose(final Closeable closeableOne) {
        if (closeableOne != null) {
            try {
                closeableOne.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Read a text file into string array
     * @param file the file to be read, must not be null
     * @param charSet the charset to be used to decode strings, must not be null
     * @return a string array contains each text line as an element
     * @throws IOException it will be thrown if there is any transport problem
     */
    public static String[] readTextFileAsStringArray(final File file, final String charSet) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
        final List<String> readString = new ArrayList<String>(256);
        try {
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                readString.add(line);
            }
        } finally {
            silentlyClose(reader);
        }
        return readString.toArray(new String[readString.size()]);
    }

    /**
     * Break a sold string to lines
     * @param text a sold string to be broken, must not be null
     * @return a string array where each line as an array element
     */
    public static String[] breakToLines(final String text) {
        return text.split("\\n");
    }

    /**
     * Read a text (UTF-8 encoded) resource in a class path of a class 
     * @param thisClass the class to be used to get a class path, must not be null
     * @param resource the resource name, must not be null
     * @return the loaded text as a solid string
     * @throws IOException it will be thrown if there is any transport error
     * @throws FileNotFoundException it will be thrown if the resource is not found
     */
    public static String readTextResource(final Class<?> thisClass, final String resource) throws IOException {
        final InputStream file = thisClass.getResourceAsStream(resource);
        if (file == null) {
            throw new FileNotFoundException("Can't find resource " + resource);
        }
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
        try {
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line).append(NEXT_LINE);
            }
            return builder.toString();
        } finally {
            silentlyClose(reader);
        }

    }

    /**
     * Concatenate string arrays into a string array
     * @param arrays string arrays, must not be null
     * @return a string array contains all content of arrays as the arguments
     */
    public static String[] concatStringArrays(final String[]... arrays) {
        Assert.assertNotNull("Concatenated arrays must not contain null", (Object[]) arrays);
        final List<String> result = new ArrayList<String>();
        for (final String[] arg : arrays) {
            for (final String s : arg) {
                result.add(s);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Convert an integer into an ASM HEX representation
     * @param value an integer value to be converted
     * @return a hex string representation of the integer
     */
    public static String intToString(final int value) {
        final StringBuilder result = new StringBuilder();

        result.append(value).append("(#").append(Integer.toHexString(value).toUpperCase()).append(')');

        return result.toString();
    }

    /**
     * Convert a long into an ASM HEX representation
     *
     * @param value a long value to be converted
     * @return a hex string representation of the long
     */
    public static String longToString(final long value) {
        final StringBuilder result = new StringBuilder();

        result.append(value).append("(#").append(Long.toHexString(value).toUpperCase()).append(')');

        return result.toString();
    }

    /**
     * Convert a byte array into hex sequence like [#01 #02 #03]
     * @param byteArray a byte array to be converted, must not be null 
     * @return a string represents the array as a hex values
     */
    public static String arrayToHexString(final byte[] byteArray) {
        final StringBuilder result = new StringBuilder();
        result.append('[');
        boolean space = false;
        for (final byte b : byteArray) {
            if (space) {
                result.append(' ');
            } else {
                space = true;
            }

            final String byteAsHex = Integer.toHexString(b & 0xFF).toUpperCase();

            result.append('#');
            if (byteAsHex.length() == 1) {
                result.append('0');
            }
            result.append(byteAsHex);
        }
        result.append(']');

        return result.toString();
    }

    /**
     * Convert a byte array into asm compatible representation (DEFB) with limit for values per line
     * @param firstLine the first line for the result text block, it can be null
     * @param array a byte array to be converted, must not be null
     * @param maxNumbersPerString the number of values allowed per a line, if -1 then it will be default value
     * @return a string of lines converted byte values into asm compatible representation
     */
    public static String[] byteArrayToAsm(final String firstLine, final byte[] array, final int maxNumbersPerString) {

        final StringBuilder buffer = new StringBuilder(firstLine == null ? "" : firstLine);
        if (firstLine != null) {
            buffer.append("\n");
        }
        final int maxPerString = maxNumbersPerString <= 0 ? 32 : maxNumbersPerString;
        int len = array.length;
        int index = 0;
        while (len > 0) {
            int stringIntemCounter = 0;
            buffer.append("DEFB ");
            while (len > 0 && stringIntemCounter < maxPerString) {
                if (stringIntemCounter > 0) {
                    buffer.append(',');
                }
                buffer.append('#').append(Integer.toHexString(array[index++] & 0xFF).toUpperCase(Locale.ENGLISH));

                stringIntemCounter++;
                len--;
            }
            buffer.append('\n');
        }

        return breakToLines(buffer.toString());
    }
    
    
    private static final AntPathMatcher ANT_MATCHER = new AntPathMatcher();
    
    /**
     * Check a path for its compatibility with a ANT styled pattern
     * @param path a path to be checked, must not be null
     * @param antPattern a pattern to be used to check the path, must not be null
     * @return true if the path is compatible with the pattern
     */
    public static boolean checkPathForAntPattern(final String path, final String antPattern) {
        String newPath = path.startsWith("/") ? path : '/' + path;
        return ANT_MATCHER.match(antPattern.toLowerCase(Locale.ENGLISH), newPath.toLowerCase(Locale.ENGLISH));
    }
}
