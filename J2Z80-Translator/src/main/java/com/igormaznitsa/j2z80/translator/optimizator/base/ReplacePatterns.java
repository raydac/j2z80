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
package com.igormaznitsa.j2z80.translator.optimizator.base;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.translator.optimizator.AsmOptimizer;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Optimizer for assembler to replace found patterns
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ReplacePatterns implements AsmOptimizer {
    private static final String[] NONE = new String[0];
    private static final String[] CLRLOC = new String[]{"CLRLOC"};

    private enum OptimizationState {
        PUSH_POP_BC(new String[]{"PUSH BC","POP BC"},NONE),
        PUSH_CLRLOC_POP_BC(new String[]{"PUSH BC","CLRLOC","POP BC"},CLRLOC),
        
        PUSH_POP_DE(new String[]{"PUSH DE","POP DE"},NONE),
        PUSH_CLRLOC_POP_DE(new String[]{"PUSH DE","CLRLOC","POP DE"},CLRLOC),
        
        PUSH_POP_HL(new String[]{"PUSH HL","POP HL"},NONE),
        PUSH_CLRLOC_POP_HL(new String[]{"PUSH HL","CLRLOC","POP HL"},CLRLOC),
        
        PUSH_POP_AF(new String[]{"PUSH AF","POP AF"},NONE),
        PUSH_CLRLOC_POP_AF(new String[]{"PUSH AF","CLRLOC","POP AF"},CLRLOC),
        
        PUSH_POP_IX(new String[]{"PUSH IX","POP IX"},NONE),
        PUSH_CLRLOC_POP_IX(new String[]{"PUSH IX","CLRLOC","POP IX"},CLRLOC),
        
        PUSH_POP_IY(new String[]{"PUSH IY","POP IY"},NONE),
        PUSH_CLRLOC_IY(new String[]{"PUSH IY","CLRLOC","POP IY"},CLRLOC);

        private final List<ParsedAsmLine> theCase;
        private final List<ParsedAsmLine> replacement;
        
        private OptimizationState(final String [] theCase, final String [] replacement){
            this.theCase = new ArrayList<ParsedAsmLine>(theCase.length);
            for(final String str : theCase){
                this.theCase.add(new ParsedAsmLine(str));
            }

            this.replacement = new ArrayList<ParsedAsmLine>(theCase.length);
            for(final String str : replacement){
                this.replacement.add(new ParsedAsmLine(str));
            }
        }
        
        public boolean process(final List<ParsedAsmLine> lines){
            boolean changed = false;
            
            while(true){
                final int index = Collections.indexOfSubList(lines, theCase);
                if (index>=0){
                    changed = true;
                    int count = theCase.size();
                    while(count!=0) {
                        lines.remove(index);
                        count--;
                    }
                    if (!replacement.isEmpty()){
                        for(int i=0;i<replacement.size();i++){
                            lines.add(index+i, replacement.get(i));
                        }
                    }
                } else {
                    break;
                }
            }
            
            return changed;
        }
    }
    
    @Override
    public List<ParsedAsmLine> optimizeAsmText(final TranslatorContext context, final List<ParsedAsmLine> lines) {
        final List<ParsedAsmLine> result = new LinkedList<ParsedAsmLine>(lines);
        
        boolean loop = true;
        
        while(loop){
            loop = false;
            for(final OptimizationState optCase : OptimizationState.values()){
                if (optCase.process(result)){
                    loop = true;
                    break;
                }
            }
        }        
        
        return result;
    }

 
}
