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

import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.ids.*;
import java.util.Map.Entry;
import java.util.*;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

class MethodContextImpl implements MethodContext {
    private final TranslatorImpl theTranslator;
    private final Map<MethodID, ClassMethodInfo> methodIds = new HashMap<MethodID, ClassMethodInfo>();

    
    public MethodContextImpl(final TranslatorImpl translator) {
        this.theTranslator = translator;
    }

    public List<MethodID> init(){
        int methodIdCounter = 0;

        final List<MethodID> methodsToProcess = new ArrayList<MethodID>();

        for (final ClassGen c : theTranslator.workingClassPath.getAllClasses().values()) {
            for (final Method m : c.getMethods()) {
                final MethodID methodId = new MethodID(c, m);
                methodIds.put(methodId, new ClassMethodInfo(c, m, methodIdCounter));
                methodIdCounter++;

                if (!(c.isInterface() || c.isEnum()) && !(m.isNative() || m.isAbstract())) {
                    methodsToProcess.add(methodId);
                }
            }
        }

        return Collections.unmodifiableList(methodsToProcess);
    }

    Set<Entry<MethodID,ClassMethodInfo>> getMethods(){
        return methodIds.entrySet();
    }
    
    @Override
    public ClassMethodInfo findMethodInfo(final MethodID mid) {
        return methodIds.get(mid);
    }

    @Override
    public Integer findMethodUID(final MethodID methodId) {
        final ClassMethodInfo info = methodIds.get(methodId);
        if (info != null) {
            return Integer.valueOf(info.getUID());
        }
        return null;
    }

    @Override
    public MethodGen findMethod(final MethodID methodId) {
        ClassMethodInfo info = methodIds.get(methodId);
        if (info != null) {
            return info.getMethodGen();
        }

        // find the method between ancestors because it can be inherited
        info = findInheritedMethod(methodId);
        if (info != null) {
            return info.getMethodGen();
        }
        return null;
    }
    
    public ClassMethodInfo findInheritedMethod(final MethodID method) {
        final String className = method.getClassName();
        ClassGen cgen = theTranslator.workingClassPath.findClassForName(className);
        if (cgen == null) {
            return null;
        }
        cgen = theTranslator.workingClassPath.findClassForName(cgen.getSuperclassName());

        while (cgen != null) {
            final Method compatibleMethod = method.findCompatibleMethod(cgen);
            if (compatibleMethod != null) {
                final MethodID thatMethodId = new MethodID(cgen, compatibleMethod);
                return methodIds.get(thatMethodId);
            }
            cgen = theTranslator.workingClassPath.findClassForName(cgen.getSuperclassName());
        }
        return null;
    }
    
}
