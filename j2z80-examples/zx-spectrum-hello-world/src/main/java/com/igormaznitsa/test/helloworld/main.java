package com.igormaznitsa.test.helloworld;

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
      showZoom();
      waitForSpace();
      showCurves();
      waitForSpace();
    }
  }

  private static void greet() {
    Screen.paper(Screen.WHITE);
    Screen.ink(Screen.BLACK);
    Screen.bright(0);
    Screen.flash(0);
    Screen.clear();
    Screen.border(Screen.GREEN);
    Screen.at(10, 8);
    System.out.println("Hello world 2026");
    Screen.lowerColors(Screen.YELLOW, Screen.BLUE, 1, 0);
    Screen.clearLower();
    System.err.println("SPACE draws the set");
    Sound.tone(80, 400);
  }

  private static void showOverview() {
    openPicture(Screen.BLUE);
    paint(Mandelbrot.OVERVIEW_LEFT, Mandelbrot.OVERVIEW_TOP, Mandelbrot.OVERVIEW_STEP);
    Screen.border(Screen.BLUE);
    caption("Overview   SPACE");
    Sound.tone(80, 300);
  }

  private static void showZoom() {
    openPicture(Screen.RED);
    paint(Mandelbrot.ZOOM_LEFT, Mandelbrot.ZOOM_TOP, Mandelbrot.ZOOM_STEP);
    Screen.border(Screen.RED);
    caption("Seahorse   SPACE");
    Sound.tone(120, 220);
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
