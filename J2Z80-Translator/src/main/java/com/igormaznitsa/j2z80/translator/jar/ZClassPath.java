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

import com.igormaznitsa.j2z80.TranslatorContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;

/**
 * The class implements inside virtual class and resource storage for the translator. 
 * The Storage is formed through parsing all jar files and extract all their classes and resources.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ZClassPath {

    private final Map<String, ClassGen> classStorage = new HashMap<String, ClassGen>();
    private final Map<String, byte[]> jniCodeStorage = new HashMap<String, byte[]>();
    private final Map<String, byte[]> binaryDataStorage = new HashMap<String, byte[]>();
    private final TranslatorContext context;
    private final ZParsedJar [] jarFiles;
    private ClassGen mainClass;
    private Method mainMethod;
    
    public ZClassPath(final TranslatorContext context, final ZParsedJar... classPath) {
        this.context = context;
        this.jarFiles = classPath;

        for (final ZParsedJar archive : classPath) {
            processArchive(archive);
        }

    }

    public ClassGen findMainClass(final String mainClassName, final String mainMethod, final String mainMethodSignature) {
        if (mainClassName != null) {
            this.mainClass = classStorage.get(mainClassName);
            if (this.mainClass != null) {
                this.mainMethod = findMainMethodInClass(mainClass, mainMethod, mainMethodSignature);
            }
            return this.mainClass;
        }

        for (int i = this.jarFiles.length-1; i >= 0; i--) {
            final ZParsedJar processingJar = this.jarFiles[i];
            for (final ClassGen cgen : processingJar.getAllJavaClasses()) {
                final Method foundMethod = findMainMethodInClass(cgen, mainMethod, mainMethodSignature);
                if (foundMethod != null && isClassPresented(cgen)) {
                    this.mainClass = cgen;
                    this.mainMethod = foundMethod;
                    return this.mainClass;
                }
            }
        }
        
        return null;
    }

    private boolean isClassPresented(final ClassGen classData) {
        for (final ClassGen cgen : classStorage.values()) {
            if (cgen == classData) {
                return true;
            }
        }
        return false;
    }

    private Method findMainMethodInClass(final ClassGen classGen, final String mainMethodName, final String mainMethodSignature) {
        if (!classGen.isInterface() && !classGen.isEnum()) {
            for (final Method method : classGen.getMethods()) {
                if (method.isStatic() && mainMethodName.equals(method.getName()) && mainMethodSignature.equals(method.getSignature())) {
                    return method;
                }
            }
        }
        return null;
    }

    public ClassGen getMainClass() {
        return mainClass;
    }

    public Method getMainMethod() {
        return mainMethod;
    }

    private void processArchive(final ZParsedJar archive) {
        processClasses(archive);
        processJniCode(archive);
        processBinaryResource(archive);
    }

    private void processClasses(final ZParsedJar archive) {
        for (final ClassGen classData : archive.getAllJavaClasses()) {
            final String className = classData.getClassName();

            if (classStorage.containsKey(className)) {
                context.getLogger().logWarning("Detected overrided class " + className);
            }
            classStorage.put(className, classData);
        }
    }

    private void processJniCode(final ZParsedJar archive) {
        for (final Entry<String, byte[]> jniData : archive.getAllJNIData().entrySet()) {
            final String resName = jniData.getKey();
            if (jniCodeStorage.containsKey(resName)) {
                context.getLogger().logWarning("Detected overrided JNI resource " + resName);
            }
            jniCodeStorage.put(resName, jniData.getValue());
        }
    }

    private void processBinaryResource(final ZParsedJar archive) {
        for (final Entry<String, byte[]> binData : archive.getAllBinaryResources().entrySet()) {
            final String resName = binData.getKey();
            if (binaryDataStorage.containsKey(resName)) {
                context.getLogger().logWarning("Detected overrided binary resource " + resName);
            }
            binaryDataStorage.put(resName, binData.getValue());
        }
    }

    public byte [] findJNICodeForPath(final String path){
        return jniCodeStorage.get(path);
    }
    
    public byte [] findNonClassForPath(final String path){
        byte [] result = jniCodeStorage.get(ZParsedJar.normalizeEntryPath(path));
        if (result == null){
            result = binaryDataStorage.get(path);
        }
        return result;
    }
    
    public Map<String, ClassGen> getAllClasses() {
        return Collections.unmodifiableMap(classStorage);
    }

    public ClassGen findClassForName(final String name) {
        return classStorage.get(name);
    }

    public Map<String, byte []> getAllBinaryResources() {
            return Collections.unmodifiableMap(binaryDataStorage);
    }
}
