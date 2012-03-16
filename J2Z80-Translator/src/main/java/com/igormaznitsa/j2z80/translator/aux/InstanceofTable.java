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
import com.igormaznitsa.j2z80.ids.ClassID;
import java.util.*;
import org.apache.bcel.generic.ClassGen;

public final class InstanceofTable {
    public final static class InstanceofRow {
        private final ClassID classId;
        private final Set<ClassID> compatibleClasses = new HashSet<ClassID>();
        
        public InstanceofRow(final ClassID classId){
            this.classId = classId;
        }
        
        public ClassID getClassID(){
            return this.classId;
        }
        
        public void addClass(final ClassID classId){
            compatibleClasses.add(classId);
        }
        
        public void addClasses(final ClassID ... ids){
            for(final ClassID i : ids){
               addClass(i);
            }
        }
        
        public String toAsm(){
            final StringBuilder result = new StringBuilder();
            result.append("DEFW ").append(LabelUtils.makeLabelForClassID(classId)).append("\n");
            
            result.append("DEFB ").append(compatibleClasses.size()).append("\n");
            result.append("DEFW ");
            boolean first = true;
            for(final ClassID compatibleClass : compatibleClasses){
                if (!first){
                    result.append(',');
                }
                result.append(LabelUtils.makeLabelForClassID(compatibleClass));
                first = false;
            }
            return result.toString();
        }
        
        @Override
        public int hashCode(){
            return classId.hashCode();
        }
        
        @Override
        public boolean equals(final Object obj){
            if (obj == null || obj.getClass() != InstanceofRow.class){
                return false;
            }
            
            final InstanceofRow thatRow = (InstanceofRow)obj;
            
            return this.classId.equals(thatRow.classId);
        }
    }
    
    private final List<InstanceofRow> rows = new ArrayList<InstanceofRow>();
    
    public InstanceofTable(final TranslatorContext translator, final Set<ClassID> classesToBeChecked){
        final ClassContext classContext = translator.getClassContext();
        
        for(final ClassID c : classesToBeChecked){
            final InstanceofRow row = addRow(c);
            
            final ClassGen classGen = classContext.findClassForID(c);
            
            final List<String> allSuccesors = classContext.findAllClassSuccessors(classGen.getClassName());
            final Set<ClassID> allImplementingInterface = classContext.findAllClassesImplementInterface(classGen.getClassName());
            
            final Set<ClassID> allCompatible = new HashSet<ClassID>();
            for(final String s : allSuccesors){
                allCompatible.add(new ClassID(s));
            }
            allCompatible.addAll(allImplementingInterface);
            
            row.addClass(c);
            for(final ClassID cc : allCompatible){
                row.addClass(cc);
            }
        }
    }
    
    public void clear(){
        rows.clear();
    }
    
    public int size(){
        return rows.size();
    }
    
    public InstanceofRow addRow(final ClassID classId){
        InstanceofRow row = new InstanceofRow(classId);
        
        final int existRowIndex = rows.indexOf(row);
        if (existRowIndex<0){
            rows.add(row);
        } else {
            row = rows.get(existRowIndex);
        }
        
        return row;
    }
    
    public String toAsm(){
        final StringBuilder result = new StringBuilder();
        result.append("DEFB ").append(rows.size()).append('\n');
        for(final InstanceofRow row : rows){
            result.append(row.toAsm()).append('\n');
        }
        return result.toString();
    }
}
