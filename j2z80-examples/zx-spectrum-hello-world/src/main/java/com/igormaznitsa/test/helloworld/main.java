package com.igormaznitsa.test.helloworld;

import j2z80.ZSystem;
import j2z80.spectrum.Keyboard;
import j2z80.spectrum.Screen;
import j2z80.spectrum.Sound;

public class main {

  public static void mainz() {
    while (true) {
      greet();
      waitForSpace();
      showOverview();
      waitForSpace();
      showStars();
      showCurves();
      waitForSpace();
    }
  }

  private static void greet() {
    Screen.colors(Screen.YELLOW, Screen.BLUE, 0, 0);
    Screen.clear();
    Screen.clearPixels();
    Screen.border(Screen.GREEN);
    Portrait.draw(Portrait.PANORAMA);
    Screen.border(Screen.YELLOW);
    Portrait.draw(Portrait.PORTRAIT);
    Screen.border(Screen.GREEN);
    Portrait.inverse();
    caption("press space (top #");
    printAddress(ZSystem.top());
    Screen.print(')');
    Sound.tone(80, 400);
  }

  private static void showOverview() {
    openPicture(Screen.BLUE);
    paint(Mandelbrot.OVERVIEW_LEFT, Mandelbrot.OVERVIEW_TOP, Mandelbrot.OVERVIEW_STEP);
    Screen.border(Screen.BLUE);
    caption("Overview   SPACE");
    Sound.tone(80, 300);
  }

  private static void showStars() {
    Screen.colors(Screen.WHITE, Screen.BLACK, 1, 0);
    Screen.clear();
    Screen.clearPixels();
    Screen.border(Screen.CYAN);
    StarField sky = new StarField();
    sky.draw();
    Screen.border(Screen.CYAN);
    caption("Stars      SPACE");
    Sound.tone(90, 280);
    releaseSpace();

    while (Keyboard.down(Keyboard.ROW_SPACE, Keyboard.BIT_0) == 0) {
      sky.step();
      Screen.frame();
    }

    ZSystem.forget(sky);
  }

  private static void showCurves() {
    Screen.colors(Screen.WHITE, Screen.BLUE, 1, 0);
    Screen.clear();
    Screen.clearPixels();
    Screen.border(Screen.MAGENTA);
    Curves.drawSine();
    Curves.drawLissajous();
    Screen.border(Screen.MAGENTA);
    caption("Curves     SPACE");
    Sound.tone(60, 180);
  }

  private static void openPicture(final int borderColor) {
    Screen.colors(Screen.WHITE, Screen.BLACK, 0, 0);
    Screen.clear();
    Screen.clearPixels();
    Screen.border(borderColor);
  }

  private static void paint(final int left, final int top, final int step) {
    int ci = top;
    int row = 0;

    while (row < Screen.ROWS - 2) {
      int cr = left;
      int column = 0;

      while (column < 32) {
        Screen.cell(column, row, Mandelbrot.color(Mandelbrot.escape(cr, ci)));
        cr = cr + step;
        column = column + 1;
      }

      Screen.border(row & 7);
      ci = ci - step;
      row = row + 1;
    }
  }

  private static void caption(final String text) {
    Screen.lowerColors(Screen.YELLOW, Screen.BLUE, 1, 0);
    Screen.clearLower();
    System.err.println(text);
  }

  private static void printAddress(final int address) {
    Screen.print(hexDigit(address, 12));
    Screen.print(hexDigit(address, 8));
    Screen.print(hexDigit(address, 4));
    Screen.print(hexDigit(address, 0));
  }

  static int hexDigit(final int address, final int shift) {
    final int nibble = (address >> shift) & 15;
    return nibble > 9 ? 'A' + (nibble - 10) : '0' + nibble;
  }

  private static void waitForSpace() {
    releaseSpace();

    int lit = 0;
    int tick = 0;

    while (Keyboard.down(Keyboard.ROW_SPACE, Keyboard.BIT_0) == 0) {
      tick = tick + 1;
      if (tick == 8) {
        tick = 0;
        lit = toggleMarker(lit);
      }
      Screen.frame();
    }
  }

  private static void releaseSpace() {
    while (Keyboard.down(Keyboard.ROW_SPACE, Keyboard.BIT_0) != 0) {
      Screen.frame();
    }
  }

  private static int toggleMarker(final int lit) {
    int next = 0;
    if (lit == 0) {
      Screen.plot(128, 0);
      next = 1;
    } else {
      Screen.unplot(128, 0);
    }

    if (Screen.point(128, 0) != next) {
      Screen.border(Screen.WHITE);
    }
    return next;
  }
}
