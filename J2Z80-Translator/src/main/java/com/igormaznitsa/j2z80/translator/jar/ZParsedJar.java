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
package com.igormaznitsa.j2z80.translator.jar;

import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.j2z80.translator.aux.ClassUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;

/**
 * The class allows to parse a Jar file and extract its entries.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ZParsedJar {

    private final JarFile jarFile;

    private final List<ClassGen> classes = new ArrayList<ClassGen>();
    private final Map<String, byte []> nativeCodeFiles = new HashMap<String,byte[]>();
    private final Map<String,byte[]> binaryResources = new HashMap<String, byte[]>();
    
    public ZParsedJar(final File jarFile) throws IOException {
        this.jarFile = new JarFile(jarFile);
        extractAll();
    }

    private void extractAll() throws IOException{
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                final byte[] entryData = extractEntry(entry);
                final String name = normalizeEntryPath(entry.getName());
                if (isJavaClass(entry)){
                    final ClassGen classGen = new ClassGen(new ClassParser(new ByteArrayInputStream(entryData), entry.getName()).parse());
                    final String message = ClassValidator.validateClass(classGen);
                    if (message != null) {
                        throw new IOException("Disallowed class detected " + classGen.getClassName() + " [" + message + ']');
                    }
                    classes.add(classGen);
                } else if (isNativeCodeFile(entry)) {
                    nativeCodeFiles.put(name, entryData);
                } else {
                    binaryResources.put(name, entryData);
                }
            }
        }
    }
    
    public List<ClassGen> getAllJavaClasses(){
        return Collections.unmodifiableList(classes);
    }
    
    public Map<String,byte[]> getAllJNIData(){
        return Collections.unmodifiableMap(nativeCodeFiles);
    }
    
    public Map<String,byte[]> getAllBinaryResources(){
        return Collections.unmodifiableMap(binaryResources);
    }
    
    public static String normalizeEntryPath(final String path){
        String name = path;
        name = name.replace('\\', '/');
        if (name.startsWith("/")) name = name.substring(1);
        return name;
    }
    
    private boolean isJavaClass(final JarEntry entry){
        return !entry.isDirectory() && entry.getName().endsWith(".class");
    }
    
    private boolean isNativeCodeFile(final JarEntry entry){
        if (!entry.isDirectory()){
            final String entryName = entry.getName();
            return ClassUtils.isAllowedNativeSourceCodeName(entryName) || ClassUtils.isAllowedCompiledNativeCodeName(entryName);
        }
        return false;
    }
    
    public byte[] extractEntry(final JarEntry entry) throws IOException {
        int length = (int) entry.getSize();
        final byte[] buffer = new byte[(int) entry.getSize()];
        final InputStream inStream = jarFile.getInputStream(entry);
        try {
            int pos = 0;
            while (length > 0) {
                final int read = inStream.read(buffer, pos, length);
                pos += read;
                length -= read;
            }
        }
        finally {
            Utils.silentlyClose(inStream);
        }
        return buffer;
    }

}
