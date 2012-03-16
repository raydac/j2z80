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

import java.io.*;
import java.util.*;

public final class Utils {

    private Utils(){
    }
       
    public static final String NEXT_LINE = "\r\n";    
        
    public static void silentlyClose(final Closeable closeableOne){
        try {
            closeableOne.close();
        }catch(IOException ex){
        }
    }
    
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

    public static String [] breakToLines(final String text){
        return text.split("\\n");
    }
    
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
        }
        finally {
            silentlyClose(reader);
        }

    }


    public static String[] concatStringArrays(final String [] ... arrays) {
        Assert.assertNotNull("Concatenated arrays must not contain null", (Object[]) arrays);
        final List<String> result = new ArrayList<String>();
        for(final String [] arg : arrays) {
            for(final String s: arg){
                result.add(s);
            }
        }
        return result.toArray(new String [result.size()]);
    }

    public static String intToString(final int value) {
        final StringBuilder result = new StringBuilder();
        
        result.append(value).append("(#").append(Integer.toHexString(value).toUpperCase()).append(')');
        
        return result.toString();
    }
    
    public static String longToString(final long value) {
        final StringBuilder result = new StringBuilder();
        
        result.append(value).append("(#").append(Long.toHexString(value).toUpperCase()).append(')');
        
        return result.toString();
    }
    
    public static String arrayToHexString(final byte[] machineCode) {
        final StringBuilder result = new StringBuilder();
        result.append('[');
        boolean space = false;
        for(final byte b : machineCode){
            if (space){
                result.append(' ');
            } else {
                space = true;
            }
            
            final String byteAsHex = Integer.toHexString(b & 0xFF).toUpperCase();
            
            result.append('#');
            if (byteAsHex.length()==1){
                result.append('0');
            }
            result.append(byteAsHex);
        }
        result.append(']');
        
        return result.toString();
    }
}
