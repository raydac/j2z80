package j2z80.spectrum;

public class Keyboard {

  public static final int ROW_SHIFT = 0;
  public static final int ROW_ASDFG = 1;
  public static final int ROW_QWERT = 2;
  public static final int ROW_12345 = 3;
  public static final int ROW_09876 = 4;
  public static final int ROW_POIUY = 5;
  public static final int ROW_ENTER = 6;
  public static final int ROW_SPACE = 7;

  public static final int BIT_0 = 1;
  public static final int BIT_1 = 2;
  public static final int BIT_2 = 4;
  public static final int BIT_3 = 8;
  public static final int BIT_4 = 16;

  private Keyboard() {
  }

  public static native int row(final int rowIndex);

  public static int down(final int rowIndex, final int mask) {
    return (row(rowIndex) & mask) != 0 ? 1 : 0;
  }
}
