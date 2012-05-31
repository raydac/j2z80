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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 * The class allows to process classes containing JNI methods.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class NativeClassProcessor {

    private final TranslatorContext theTranslator;

    public NativeClassProcessor(final TranslatorContext translator) {
        this.theTranslator = translator;
    }

    public String[] findNativeSources(final ClassMethodInfo classInfo) throws IOException {
        final String packageName = classInfo.getPackageName();
        final String onlyClassName = classInfo.getOnlyClassName();
        final String path = packageName.replace('.', '/');

        // find the whole class
        final String[] jniWholeClass = readNativeResource(path, onlyClassName);

        // find resources for each native method
        final Map<Method, String[]> jniMethodBodies = new HashMap<Method, String[]>();
        final Set<String> jniMethodNames = new HashSet<String>();
        for (final Method method : classInfo.getClassInfo().getMethods()) {
            if (!method.isNative()) {
                continue;
            }
            final String methodName = method.getName();

            final String resourceName = onlyClassName + '#' + methodName;

            final String[] methodBody = readNativeResource(path, resourceName);
            if (methodBody != null) {
                if (jniMethodNames.contains(methodName)) {
                    final String errorMessage = "Found two or more JNI methods in " + classInfo.getCanonicalClassName() + " named " + methodName + " and one of them has the separated JNI file as the body. In the case the class must have only JNI method with the name.";
                    theTranslator.getLogger().logError(errorMessage);
                }
                jniMethodBodies.put(method, methodBody);
                jniMethodNames.add(methodName);
            }
        }

        // make the result
        // as the first we add the whole class body if it is presented
        final List<String> result = new ArrayList<String>(1024);
        
        result.add("");
        result.add("; -------- [JNI] START OF " + classInfo.getCanonicalClassName()+" --------");
        if (jniWholeClass != null) {
            for (final String str : jniWholeClass) {
                result.add(str);
            }
        }

        // next we add all found method bodies and generate labels for them
        if (!jniMethodBodies.isEmpty()){
            for(final Map.Entry<Method,String[]> methodEntry : jniMethodBodies.entrySet()){
                final Method method = methodEntry.getKey();
                final String methodLabel = LabelAndFrameUtils.makeLabelNameForMethod(classInfo.getClassInfo().getClassName(),method.getName(),method.getReturnType(),method.getArgumentTypes());
                
                result.add(methodLabel+':');
            
                for(final String str : methodEntry.getValue()) result.add(str);
            }
        }
        result.add("; -------- [JNI] END OF " + classInfo.getCanonicalClassName() + " --------");
        result.add("");
        return result.toArray(new String[result.size()]);
    }

    private static String [] insertFirstStringIntoArray(final String str, final String [] array){
        final String [] result = new String[array.length+1];
        System.arraycopy(array, 0, result, 1, array.length);
        result[0] = str;
        return result;
    }
    
    private String[] readNativeResource(final String path, final String resourceName) throws IOException {
        byte[] result = null;
        final String filePath = path + '/' + resourceName;

        String readResourcePath = null;
        
        // find between asm files
        for (final String ext : ClassUtils.ALLOWED_JNI_ASM_EXTENSIONS) {
            readResourcePath = filePath + ext;
            result = theTranslator.loadResourceForPath(readResourcePath);
            if (result != null) {
                break;
            }
        }

        if (result != null) {
            return insertFirstStringIntoArray("; file "+readResourcePath, Utils.breakToLines(new String(result, "UTF8")));
        }

        // find between bin files
        for (final String ext : ClassUtils.ALLOWED_JNI_BIN_EXTENSIONS) {
            readResourcePath = filePath + ext;
            result = theTranslator.loadResourceForPath(readResourcePath);
            if (result != null) {
                break;
            }
        }

        if (result != null) {
            final String[] data = Utils.byteArrayToAsm("; file " + readResourcePath, result, -1);
            return data;
        }

        return null;
    }
}
