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
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import java.io.IOException;

/**
 * The class allows to process classes containing JNI methods.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class NativeClassProcessor {

    private final TranslatorContext theTranslator;
    
    public NativeClassProcessor(final TranslatorContext translator){
        this.theTranslator = translator;
    }
    
    public String [] findNativeSources(final ClassMethodInfo classInfo) throws IOException {
        final String packageName = classInfo.getPackageName();
        final String className = classInfo.getOnlyClassName();

        final String fileName = className;
        final String path = packageName.replace('.', '/');
        final String filePath = path + '/' + fileName;

        byte[] result = null;

        // find between asm files
        for (final String ext : ClassUtils.ALLOWED_JNI_ASM_EXTENSIONS) {
            result = theTranslator.loadResourceForPath(filePath+ext);
            if (result != null) {
                break;
            }
        }

        if (result != null) {
            return Utils.breakToLines(new String(result, "UTF8"));
        }

        // find between bin files
        for (final String ext : ClassUtils.ALLOWED_JNI_BIN_EXTENSIONS) {
            result = theTranslator.loadResourceForPath(filePath+ext);
            if (result != null) {
                break;
            }
        }

        if (result!=null){
       
            final String [] data = Utils.byteArrayToAsm(" jni resource "+filePath,result, -1);
            return data;
        }
        
        final String errorMessage = "Can't find JNI resource for the class" + packageName + "." + className+" [JNI resource must have the path-name prefix "+filePath+']';
        theTranslator.getLogger().logError(errorMessage);
        throw new IOException(errorMessage);
    }

 }
