package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class Mandelbrot {

  public static final int LIMIT = 14;
  public static final int OVERVIEW_LEFT = -128;
  public static final int OVERVIEW_TOP = 66;
  public static final int OVERVIEW_STEP = 6;
  public static final int ZOOM_LEFT = -64;
  public static final int ZOOM_TOP = 26;
  public static final int ZOOM_STEP = 1;

  private static final int SCALE_SHIFT = 6;
  private static final int ESCAPE_SQUARE = 256;
  private static final int SAFE = 170;

  private Mandelbrot() {
  }

  public static int escape(final int real, final int imag) {
    int zr = 0;
    int zi = 0;
    int count = 0;

    while (count < LIMIT) {
      if (zr > SAFE || zr < -SAFE || zi > SAFE || zi < -SAFE) {
        return count;
      }

      int zr2 = (zr * zr) >> SCALE_SHIFT;
      int zi2 = (zi * zi) >> SCALE_SHIFT;
      if (zr2 + zi2 > ESCAPE_SQUARE) {
        return count;
      }

      int cross = (zr * zi) >> SCALE_SHIFT;
      zr = zr2 - zi2 + real;
      zi = cross + cross + imag;
      count = count + 1;
    }

    return count;
  }

  public static int color(final int count) {
    if (count >= LIMIT) {
      return Screen.attribute(Screen.BLACK, Screen.BLUE, 1, 0);
    }

    int bright = count > 7 ? 1 : 0;
    return Screen.attribute(Screen.BLACK, count & 7, bright, 0);
  }
}
