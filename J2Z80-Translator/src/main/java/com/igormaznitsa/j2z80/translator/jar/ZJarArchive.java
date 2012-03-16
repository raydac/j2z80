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

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.Utils;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class ZJarArchive {

    private final JarFile jarFile;
    private final Map<String, ClassGen> allFoundClasses = new HashMap<String, ClassGen>();
    private final Map<ClassGen, Method> mainMethods = new HashMap<ClassGen, Method>();

    public ZJarArchive(final File file, final String mainMethodName, final String mainMethodSignature) throws IOException {
        Assert.assertNotNull("Arguments must not contain null", file,mainMethodName,mainMethodSignature);
        jarFile = new JarFile(file);
        parseAllClassesInJar(mainMethodName, mainMethodSignature);
    }

    private void parseAllClassesInJar(final String mainMethodName, final String mainMethodSignature) throws IOException {
        allFoundClasses.clear();
        mainMethods.clear();
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                final byte[] entryData = extractEntry(entry);
                final ClassGen classGen = new ClassGen(new ClassParser(new ByteArrayInputStream(entryData), entry.getName()).parse());

                final String message = ClassValidator.validateClass(classGen);
                if (message!=null){
                    throw new IOException("Disallowed class detected "+classGen.getClassName()+" ["+message+']');
                }
                
                
                final Method mainMethod = findMainMethod(classGen, mainMethodName, mainMethodSignature);
                if (mainMethod!=null){
                    mainMethods.put(classGen, mainMethod);
                }
                
                allFoundClasses.put(classGen.getClassName(), classGen);
            }
        }
    }

    private Method findMainMethod(final ClassGen classGen, final String mainMethodName, final String mainMethodSignature) {
        if (!classGen.isInterface() && !classGen.isEnum()) {
            for (final Method method : classGen.getMethods()) {
                if (method.isStatic() && mainMethodName.equals(method.getName()) && mainMethodSignature.equals(method.getSignature())) {
                    return method;
                }
            }
        }
        return null;
    }

    public Map<ClassGen, Method> getMainMethods(){
        return Collections.unmodifiableMap(mainMethods);
    }
    
    public Map<String, ClassGen> getClasses(){
        return Collections.unmodifiableMap(allFoundClasses);
    }
    
    public ClassGen findClassForName(final String name){
        return allFoundClasses.get(name);
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

    public JarEntry findEntryForPath(final String string) {
        final ZipEntry zipEntry = this.jarFile.getEntry(string);
        if (zipEntry!= null){
            return new JarEntry(zipEntry);
        }
        return null;
    }
}
