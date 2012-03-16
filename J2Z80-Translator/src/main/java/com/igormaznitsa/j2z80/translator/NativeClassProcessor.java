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
import com.igormaznitsa.j2z80.aux.Utils;
import java.io.IOException;
import java.util.Locale;

public class NativeClassProcessor {

    private final String[] ALLOWED_ASM_EXTENSIONS = new String[]{".a80", ".asm", ".z80"};
    private final String[] ALLOWED_BIN_EXTENSIONS = new String[]{".bin"};
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
        for (final String ext : ALLOWED_ASM_EXTENSIONS) {
            result = theTranslator.loadResourceForPath(filePath+ext);
            if (result != null) {
                break;
            }
        }

        if (result != null) {
            return Utils.breakToLines(new String(result, "UTF8"));
        }

        // find between bin files
        for (final String ext : ALLOWED_BIN_EXTENSIONS) {
            result = theTranslator.loadResourceForPath(filePath+ext);
            if (result != null) {
                break;
            }
        }

        if (result!=null){
            final StringBuilder buffer = new StringBuilder("; jni resource ");
            buffer.append(filePath).append('\n');
            final int maxPerString = 32;
            int len = result.length;
            int index = 0;
            while(len>0){
                int stringIntemCounter = 0;
                buffer.append("DEFB ");
                while(len>0 && stringIntemCounter<maxPerString){
                    if (stringIntemCounter>0){
                        buffer.append(',');
                    }
                    buffer.append('#').append(Integer.toHexString(result[index++] & 0xFF).toUpperCase(Locale.ENGLISH));
                    
                    stringIntemCounter++;
                    len--;
                }
                buffer.append('\n');
            }
            
            return Utils.breakToLines(buffer.toString());
        }
        
        final String errorMessage = "Can't find JNI resource for the class" + packageName + "." + className+" [JNI resource must have the path-name prefix "+filePath+']';
        theTranslator.getLogger().logError(errorMessage);
        throw new IOException(errorMessage);
    }

 }
