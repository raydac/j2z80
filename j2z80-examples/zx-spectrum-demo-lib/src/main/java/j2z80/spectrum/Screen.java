package j2z80.spectrum;

public class Screen {

  public static final int BLACK = 0;
  public static final int BLUE = 1;
  public static final int RED = 2;
  public static final int MAGENTA = 3;
  public static final int GREEN = 4;
  public static final int CYAN = 5;
  public static final int YELLOW = 6;
  public static final int WHITE = 7;

  public static final int BRIGHT = 64;
  public static final int FLASH = 128;

  public static final int WIDTH = 256;
  public static final int HEIGHT = 192;
  public static final int COLUMNS = 32;
  public static final int ROWS = 24;

  private Screen() {
  }

  public static int attribute(final int ink, final int paper, final int bright, final int flash) {
    int packed = (ink & 7) | ((paper & 7) << 3);
    if (bright != 0) {
      packed = packed | BRIGHT;
    }
    if (flash != 0) {
      packed = packed | FLASH;
    }
    return packed;
  }

  public static native void border(final int color);

  public static native void colors(final int ink, final int paper, final int bright,
                                   final int flash);

  public static native void ink(final int color);

  public static native void paper(final int color);

  public static native void bright(final int enabled);

  public static native void flash(final int enabled);

  public static native void lowerColors(final int ink, final int paper, final int bright,
                                        final int flash);

  public static native void clear();

  public static native void clearPixels();

  public static native void clearLower();

  public static native void plot(final int x, final int y);

  public static native void plot(final int x, final int y, final int color);

  public static native void unplot(final int x, final int y);

  public static native int point(final int x, final int y);

  public static native void cell(final int column, final int row, final int attribute);

  public static native void at(final int row, final int column);

  public static native void print(final int code);

  public static native void frame();
}
