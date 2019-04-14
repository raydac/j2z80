package com.igormaznitsa.j2z80.translator.utils;

public enum AsmAssertions {
  ;

  /**
   * Check that an integer value in signed byte bounds
   *
   * @param value an integer value to be checked
   */
  public static void assertSignedByte(final int value) {
    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
      throw new IllegalArgumentException("Signed byte must be in " + Byte.MIN_VALUE + ".." + Byte.MAX_VALUE + " [" + value + ']');
    }
  }

  /**
   * Check that an integer in unsigned byte bounds
   *
   * @param value an integer value to be checked
   */
  public static void assertUnsignedByte(final int value) {
    if ((value & 0xFFFFFF00) != 0) {
      throw new IllegalArgumentException("Unsigned byte must be in 8 bit bounds [" + value + ']');
    }
  }

  /**
   * Check that an integer in signed short bounds
   *
   * @param value an integer to be checked
   */
  public static void assertSignedShort(final int value) {
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
      throw new IllegalArgumentException("Signed short must be in " + Short.MIN_VALUE + ".." + Short.MAX_VALUE + " [" + value + ']');
    }
  }

  /**
   * Check that an integer in unsigned short bounds
   *
   * @param value an integer to be checked
   */
  public static void assertUnsignedShort(final int value) {
    if ((value & 0xFFFF0000) != 0) {
      throw new IllegalArgumentException("Unsigned short must be in 16 bit bounds [" + value + ']');
    }
  }

  /**
   * Check that an integer can represent 16 bit address
   *
   * @param value an integer value to be checked
   */
  public static void assertAddress(final int value) {
    if (value < 0 || value > 0xFFFF) {
      throw new IllegalArgumentException("Address must be in the 0x0...0xFFFF interval [0x" + Integer.toHexString(value).toUpperCase() + ']');
    }
  }

  /**
   * Check that an integer has zero value
   *
   * @param msg   the message to declare that the integer must be zero
   * @param value an integer value to be checked
   */
  public static void assertZero(final String msg, final int value) {
    if (value != 0) {
      throw new IllegalStateException(msg);
    }
  }

  /**
   * Check validity of a label name
   *
   * @param labelName a label name to be checked
   */
  private static void checkLabelNameChars(final String labelName) {
    if (labelName.isEmpty()) {
      throw new IllegalArgumentException("Label must not be an empty string");
    }
    for (final char c : labelName.toCharArray()) {
      if (Character.isWhitespace(c)) {
        throw new IllegalArgumentException("Label name must not contain any kind of a white character [" + labelName + ']');
      }
    }
  }

  /**
   * Check validity of a local label name
   *
   * @param labelName a label name to be checked
   */
  public static void assertLocalLabelName(final String labelName) {
    checkLabelNameChars(labelName);
    if (!labelName.startsWith("@")) {
      throw new IllegalArgumentException("Must be a local label name, it starts with the @ symbol [" + labelName + ']');
    }
  }

  /**
   * Check validity of a global label name
   *
   * @param labelName a label name to be checked
   */
  public static void assertGlobalLabelName(final String labelName) {
    checkLabelNameChars(labelName);
    if (labelName.startsWith("@")) {
      throw new IllegalArgumentException("Must be a global label name, it must not start with the @ symbol [" + labelName + ']');
    }
  }

  /**
   * Check that an integer value is not zero
   *
   * @param msg   the information message
   * @param value an ineteger value to be checked
   */
  public static void assertNonZero(final String msg, final int value) {
    if (value == 0) {
      throw new IllegalArgumentException(msg);
    }
  }

}
