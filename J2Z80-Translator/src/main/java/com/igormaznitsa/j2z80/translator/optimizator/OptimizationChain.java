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
package com.igormaznitsa.j2z80.translator.optimizator;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.util.ArrayList;
import java.util.List;

public class OptimizationChain {
    private final TranslatorContext context;
    private final AsmOptimizator [] optimizators;
    
    protected OptimizationChain(final TranslatorContext context, final AsmOptimizator ... optimizators){
        this.context = context;
        this.optimizators = optimizators == null ? new AsmOptimizator[0] : optimizators.clone();
    }
    
    public List<ParsedAsmLine> processSources(final List<ParsedAsmLine> lines){
        List<ParsedAsmLine> processing = new ArrayList<ParsedAsmLine>(lines);
        for(final AsmOptimizator optimizator : optimizators){
            processing = optimizator.optimizeAsmText(this.context,processing);
        }
        return processing;
    }
}