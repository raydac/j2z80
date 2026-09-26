package com.igormaznitsa.test.helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import j2z80.spectrum.Screen;
import org.junit.Test;

public class MandelbrotTest {

  @Test
  public void originStaysInside() {
    assertEquals(Mandelbrot.LIMIT, Mandelbrot.escape(0, 0));
  }

  @Test
  public void periodTwoBulbStaysInside() {
    assertEquals(Mandelbrot.LIMIT, Mandelbrot.escape(-64, 0));
  }

  @Test
  public void pointOutsideEscapesImmediately() {
    assertEquals(1, Mandelbrot.escape(0, 160));
  }

  @Test
  public void realOneAndAHalfEscapes() {
    assertEquals(2, Mandelbrot.escape(96, 0));
  }

  @Test
  public void zoomCenterStaysInside() {
    assertEquals(Mandelbrot.LIMIT, Mandelbrot.escape(-48, 6));
  }

  @Test
  public void overviewWindowStaysInRange() {
    int inside = 0;
    int ci = Mandelbrot.OVERVIEW_TOP;
    int row = 0;

    while (row < Screen.ROWS - 2) {
      int cr = Mandelbrot.OVERVIEW_LEFT;
      int column = 0;

      while (column < 32) {
        int count = Mandelbrot.escape(cr, ci);
        assertTrue(count >= 0 && count <= Mandelbrot.LIMIT);
        if (count == Mandelbrot.LIMIT) {
          inside = inside + 1;
        }
        cr = cr + Mandelbrot.OVERVIEW_STEP;
        column = column + 1;
      }

      ci = ci - Mandelbrot.OVERVIEW_STEP;
      row = row + 1;
    }

    assertTrue(inside > 20);
  }

  @Test
  public void interiorIsBrightBlue() {
    assertEquals(Screen.attribute(Screen.BLACK, Screen.BLUE, 1, 0),
        Mandelbrot.color(Mandelbrot.LIMIT));
  }

  @Test
  public void attributePacksInkPaperBrightAndFlash() {
    assertEquals(234, Screen.attribute(2, 5, 1, 1));
  }
}
