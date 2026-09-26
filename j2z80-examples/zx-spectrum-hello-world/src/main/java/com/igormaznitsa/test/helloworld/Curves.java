package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class Curves {

  private Curves() {
  }

  public static float halfWave(final float radians) {
    float pi = 3.1416f;
    float product = radians * (pi - radians);
    return (16f * product) / ((5f * pi * pi) - (4f * product));
  }

  public static double turnedX(final double x, final double y, final double cosine,
                               final double sine) {
    return x * cosine - y * sine;
  }

  public static double turnedY(final double x, final double y, final double cosine,
                               final double sine) {
    return x * sine + y * cosine;
  }

  public static void drawSine() {
    int previousX = 0;
    int previousY = 96;
    int column = 0;

    while (column <= 255) {
      float angle = column * 0.024544f;
      float wave = signedWave(angle);
      int y = 96 + (int) (wave * 28f);
      if (column == 0) {
        previousX = 0;
        previousY = y;
      }
      line(previousX, previousY, column, y);
      previousX = column;
      previousY = y;
      column = column + 4;
    }
  }

  public static void drawLissajous() {
    double x3 = 1.0;
    double y3 = 0.0;
    double x2 = 1.0;
    double y2 = 0.0;
    int previousX = 128;
    int previousY = 96;
    int step = 0;

    while (step < 72) {
      int x = 128 + (int) (88.0 * y3);
      int y = 96 + (int) (64.0 * y2);
      if (step > 0) {
        line(previousX, previousY, x, y);
      }
      previousX = x;
      previousY = y;

      double nextX3 = turnedX(x3, y3, 0.965925826, 0.258819045);
      double nextY3 = turnedY(x3, y3, 0.965925826, 0.258819045);
      double nextX2 = turnedX(x2, y2, 0.984807753, 0.173648178);
      double nextY2 = turnedY(x2, y2, 0.984807753, 0.173648178);
      x3 = nextX3;
      y3 = nextY3;
      x2 = nextX2;
      y2 = nextY2;

      if ((step & 7) == 0) {
        Screen.border((step >> 3) & 7);
      }
      step = step + 1;
    }
  }

  public static void line(final int x0, final int y0, final int x1, final int y1) {
    int x = x0;
    int y = y0;
    int dx = x1 - x0;
    int dy = y1 - y0;
    int stepX = 1;
    int stepY = 1;
    if (dx < 0) {
      dx = -dx;
      stepX = -1;
    }
    if (dy < 0) {
      dy = -dy;
      stepY = -1;
    }

    int error = dx - dy;
    int guard = 0;
    while (guard < 48) {
      Screen.plot(x, y);
      if (x == x1 && y == y1) {
        return;
      }

      int doubled = error + error;
      if (doubled > -dy) {
        error = error - dy;
        x = x + stepX;
      }
      if (doubled < dx) {
        error = error + dx;
        y = y + stepY;
      }
      guard = guard + 1;
    }
  }

  private static float signedWave(final float angle) {
    float pi = 3.1416f;
    if (angle > pi) {
      return -halfWave(angle - pi);
    }
    return halfWave(angle);
  }
}
