package com.igormaznitsa.j2z80.translator.utils;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Sna48Writer {

  private static final byte[] ZX48_SNA_TEMPLATE;

  static {
    try (final InputStream is = requireNonNull(Sna48Writer.class.getResourceAsStream(
        "/com/igormaznitsa/j2z80/snapshots/zx48template.sna"))) {
      ZX48_SNA_TEMPLATE = is.readAllBytes();
    } catch (IOException ex) {
      throw new Error("unexpectedly can't load template");
    }
  }

  private final int startAddress;
  private final int stackTopAddress;
  private final byte[] data;

  public Sna48Writer(final int startAddress, final int stackTopAddress, final byte[] data) {
    if (startAddress < 0x4000 || startAddress > 0xFFFF) {
      throw new IllegalArgumentException("Illegal start address: " + startAddress);
    }
    if (stackTopAddress < 0 || stackTopAddress > 0xFFFD) {
      throw new IllegalArgumentException("Illegal stack top address: " + stackTopAddress);
    }

    this.startAddress = startAddress;
    this.stackTopAddress = stackTopAddress;
    this.data = data;
  }

  public byte[] writeSna() throws IOException {
    final byte[] result = Arrays.copyOf(ZX48_SNA_TEMPLATE, ZX48_SNA_TEMPLATE.length);

    result[0x17] = (byte) (this.stackTopAddress - 2);
    result[0x18] = (byte) ((this.stackTopAddress - 2) >> 8);

    final int offset = (0x4000 - 0x1b);

    result[this.stackTopAddress - 2 - offset] = (byte) this.startAddress;
    result[this.stackTopAddress - 1 - offset] = (byte) (this.startAddress >> 8);

    System.arraycopy(this.data, 0, result, this.startAddress - offset, this.data.length);
    return result;
  }
}
