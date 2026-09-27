package com.igormaznitsa.test.helloworld;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PortraitHeapTopTest {

  private static String hex(final int address) {
    return "" + (char) main.hexDigit(address, 12)
        + (char) main.hexDigit(address, 8)
        + (char) main.hexDigit(address, 4)
        + (char) main.hexDigit(address, 0);
  }

  @Test
  public void formatsASignedAddressAsFourHexDigits() {
    assertEquals("FAD0", hex((short) 0xFAD0));
    assertEquals("8000", hex((short) 0x8000));
    assertEquals("7FFF", hex(0x7FFF));
    assertEquals("12AB", hex(0x12AB));
    assertEquals("0000", hex(0));
  }
}
