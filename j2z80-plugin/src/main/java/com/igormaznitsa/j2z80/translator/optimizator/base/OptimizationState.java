package com.igormaznitsa.j2z80.translator.optimizator.base;

import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum OptimizationState implements OptimizationConst {

  PUSH_POP_BC(new String[] {"PUSH BC", "POP BC"}, NONE),
  PUSH_CLRLOC_POP_BC(new String[] {"PUSH BC", CLRLOC_STR, "POP BC"}, CLRLOC),
  PUSH_POP_DE(new String[] {"PUSH DE", "POP DE"}, NONE),
  PUSH_CLRLOC_POP_DE(new String[] {"PUSH DE", CLRLOC_STR, "POP DE"}, CLRLOC),
  PUSH_POP_HL(new String[] {"PUSH HL", "POP HL"}, NONE),
  PUSH_CLRLOC_POP_HL(new String[] {"PUSH HL", CLRLOC_STR, "POP HL"}, CLRLOC),
  PUSH_POP_AF(new String[] {"PUSH AF", "POP AF"}, NONE),
  PUSH_CLRLOC_POP_AF(new String[] {"PUSH AF", CLRLOC_STR, "POP AF"}, CLRLOC),
  PUSH_POP_IX(new String[] {"PUSH IX", "POP IX"}, NONE),
  PUSH_CLRLOC_POP_IX(new String[] {"PUSH IX", CLRLOC_STR, "POP IX"}, CLRLOC),
  PUSH_POP_IY(new String[] {"PUSH IY", "POP IY"}, NONE),
  PUSH_CLRLOC_IY(new String[] {"PUSH IY", CLRLOC_STR, "POP IY"}, CLRLOC);

  private final List<ParsedAsmLine> theCase;
  private final List<ParsedAsmLine> replacement;

  OptimizationState(final String[] theCase, final String[] replacement) {
    this.theCase = new ArrayList<>(theCase.length);
    for (final String str : theCase) {
      this.theCase.add(new ParsedAsmLine(str));
    }
    this.replacement = new ArrayList<>(theCase.length);
    for (final String str : replacement) {
      this.replacement.add(new ParsedAsmLine(str));
    }
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
