package com.igormaznitsa.j2z80.emulator;

import com.igormaznitsa.z80asm.Z80Asm;
import java.util.List;
import java.util.Objects;

public record Program(int origin, byte[] image, Z80Asm assembler) {

  public static Program assemble(final String source) {
    return assemble(List.of(source));
  }

  public static Program assemble(final List<String> lines) {
    final Z80Asm assembler = new Z80Asm(lines);
    final byte[] image = assembler.process();
    return new Program(assembler.getDataOffset(), image, assembler);
  }

  public int addressOf(final String label) {
    return Objects.requireNonNull(this.assembler.findLabelAddress(label), label);
  }
}
