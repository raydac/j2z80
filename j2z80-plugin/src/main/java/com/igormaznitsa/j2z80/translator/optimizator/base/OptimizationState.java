package com.igormaznitsa.j2z80.translator.optimizator.base;

import static java.util.Arrays.asList;

import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum OptimizationState implements OptimizationConst {

    PUSH_POP_BC(asList("PUSH BC", "POP BC"), NONE),
    PUSH_CLRLOC_POP_BC(asList("PUSH BC", CLRLOC_STR, "POP BC"), CLRLOC),
    PUSH_POP_DE(asList("PUSH DE", "POP DE"), NONE),
    PUSH_CLRLOC_POP_DE(asList("PUSH DE", CLRLOC_STR, "POP DE"), CLRLOC),
    PUSH_POP_HL(asList("PUSH HL", "POP HL"), NONE),
    PUSH_CLRLOC_POP_HL(asList("PUSH HL", CLRLOC_STR, "POP HL"), CLRLOC),
    PUSH_POP_AF(asList("PUSH AF", "POP AF"), NONE),
    PUSH_CLRLOC_POP_AF(asList("PUSH AF", CLRLOC_STR, "POP AF"), CLRLOC),
    PUSH_POP_IX(asList("PUSH IX", "POP IX"), NONE),
    PUSH_CLRLOC_POP_IX(asList("PUSH IX", CLRLOC_STR, "POP IX"), CLRLOC),
    PUSH_POP_IY(asList("PUSH IY", "POP IY"), NONE),
    PUSH_CLRLOC_IY(asList("PUSH IY", CLRLOC_STR, "POP IY"), CLRLOC);

    private final List<ParsedAsmLine> theCase = new ArrayList<>();
    private final List<ParsedAsmLine> replacement = new ArrayList<>();

    OptimizationState(final List<String> theCase, final List<String> replacement) {
        theCase.stream().map(ParsedAsmLine::new).forEach(this.theCase::add);
        replacement.stream().map(ParsedAsmLine::new).forEach(this.replacement::add);
    }

    public boolean process(final List<ParsedAsmLine> lines) {
        boolean changed = false;
        while (!Thread.currentThread().isInterrupted()) {
            final int index = Collections.indexOfSubList(lines, theCase);
            if (index >= 0) {
                changed = true;
                int count = theCase.size();
                while (count != 0) {
                    lines.remove(index);
                    count--;
                }
                if (!replacement.isEmpty()) {
                    for (int i = 0; i < replacement.size(); i++) {
                        lines.add(index + i, replacement.get(i));
                    }
                }
            } else {
                break;
            }
        }
        return changed;
    }

}
