package com.igormaznitsa.j2z80.emulator;

import static com.codingrodent.microprocessor.Z80.CPUConstants.RegisterNames.A;
import static com.codingrodent.microprocessor.Z80.CPUConstants.RegisterNames.BC;
import static com.codingrodent.microprocessor.Z80.CPUConstants.RegisterNames.DE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Z80CommandTest {

  @Test
  public void addCombinesRegisters() {
    final Z80Machine machine = this.run("""
        ORG #8000
        LD A,#10
        LD B,#03
        ADD A,B
        LD C,A
        HALT
        """);

    assertEquals(0x13, machine.register(A));
    assertEquals(0x0313, machine.register(BC));
  }

  @Test
  public void djnzCountsDown() {
    final Z80Machine machine = this.run("""
        ORG #8000
        LD A,#00
        LD B,#05
        COUNT: INC A
        DJNZ COUNT
        HALT
        """);

    assertEquals(0x05, machine.register(A));
    assertEquals(0x0000, machine.register(BC));
  }

  @Test
  public void callReturnsBothWords() {
    final Z80Machine machine = this.run("""
        ORG #8000
        LD SP,#F000
        CALL PAIR
        HALT
        PAIR:
        LD BC,#5678
        LD DE,#1234
        RET
        """);

    assertEquals(0x5678, machine.register(BC));
    assertEquals(0x1234, machine.register(DE));
  }

  private Z80Machine run(final String source) {
    final Program program = Program.assemble(source);
    final Z80Machine machine = new Z80Machine();
    machine.load(program);
    machine.runUntilHalt(program.origin(), 1000);
    return machine;
  }
}
