package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.j2z80.jvmprocessors.AbstractInvokeProcessor;
import org.apache.bcel.generic.Type;
import org.junit.Test;

public class Z80WideArgumentTest {

  @Test
  public void doubleArgumentsArriveLowWordFirst() {
    final String source = """
        ORG #8000
        LD SP,#F000
        LD BC,#1111
        PUSH BC
        LD BC,#0101
        PUSH BC
        LD BC,#2222
        PUSH BC
        LD BC,#0202
        PUSH BC
        """
        + AbstractInvokeProcessor.orientWideArguments(new Type[] {Type.DOUBLE, Type.DOUBLE})
        + """
        LD HL,SLOTS
        POP BC
        LD (HL),C
        INC HL
        LD (HL),B
        INC HL
        POP BC
        LD (HL),C
        INC HL
        LD (HL),B
        INC HL
        POP BC
        LD (HL),C
        INC HL
        LD (HL),B
        INC HL
        POP BC
        LD (HL),C
        INC HL
        LD (HL),B
        HALT
        SLOTS: DEFS 8
        """;

    final Program program = Program.assemble(source);
    final Z80Machine machine = new Z80Machine();
    machine.load(program);
    machine.runUntilHalt(program.origin(), 1000);

    final int slots = program.addressOf("SLOTS");
    assertEquals(0x2222, machine.wordAt(slots));
    assertEquals(0x0202, machine.wordAt(slots + 2));
    assertEquals(0x1111, machine.wordAt(slots + 4));
    assertEquals(0x0101, machine.wordAt(slots + 6));
  }
}
