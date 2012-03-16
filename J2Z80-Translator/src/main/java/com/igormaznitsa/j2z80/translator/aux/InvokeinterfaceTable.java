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
package com.igormaznitsa.j2z80.translator.aux;

import com.igormaznitsa.j2z80.*;
import com.igormaznitsa.j2z80.aux.LabelUtils;
import com.igormaznitsa.j2z80.ids.*;
import java.util.*;
import org.apache.bcel.generic.MethodGen;

@SuppressWarnings("serial")
public class InvokeinterfaceTable {
    private class Record {
        private final MethodID interfaceMethod;
        private final List<MethodID> inheritedMethods = new ArrayList<MethodID>();

        private Record(final MethodID interfaceMethod) {
             this.interfaceMethod = interfaceMethod;
        }
        
        private void addInheritedMethod(final MethodID method) {
            inheritedMethods.add(method);
        } 
        
        private String generateAsm(){
            final StringBuilder result = new StringBuilder("DEFW ");
            
            result.append(LabelUtils.makeLabelForMethodID(interfaceMethod)).append('\n');
            
            result.append("DEFB ").append(inheritedMethods.size()).append('\n');
            
            for(final MethodID method : inheritedMethods){
                final MethodGen meth = translator.getMethodContext().findMethod(method);
                final int frameSize = LabelUtils.calculateFrameSizeForMethod(meth.getArgumentTypes().length, meth.getMaxLocals(),false);
                
                result.append("DEFW ").append(LabelUtils.makeLabelForClassID(method.getClassID())).append(',')
                        .append(method.getMethodLabel()).append(',')
                        .append(frameSize)
                        .append('\n');
                        
            }
            return result.toString();
        }
    }
    
    private final TranslatorContext translator;
    private final List<Record> records = new ArrayList<Record>();
    
    public InvokeinterfaceTable(final TranslatorContext translator, final Set<MethodID> calledInterfaceMethods){
        this.translator = translator;
        final ClassContext classContext = translator.getClassContext();
        for(final MethodID method : calledInterfaceMethods){
            final Set<ClassID> successors = classContext.findAllClassesImplementInterface(method.getClassName());
            final Record newRecord = new Record(method);
            for(final ClassID s : successors){
                newRecord.addInheritedMethod(new MethodID(s.getClassName(), method.getMethodName(), method.getReturnType(), method.getArgs()));
            }
            records.add(newRecord);
        }
    }
    
    public String generateAsm(){
        final StringBuilder result = new StringBuilder("; GENERATED INVOKEINTERFACE TABLE\nDEFB ");
        result.append(records.size()).append('\n');
        for(final Record r : records){
            result.append(r.generateAsm());
        }
        return result.toString();
    }
}
