package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class StarField {

  public static final int COUNT = 256;

  public Star[] stars;

  public StarField() {
    this.stars = new Star[COUNT];

    int across = 91;
    int down = 173;

    for (int i = 0; i < COUNT; i++) {
      across = stir(across, 7, 5, 2);
      down = stir(down, 7, 3, 1);
      this.stars[i] = new Star(across & 255, down % Screen.HEIGHT, 1 + (across & 3),
          (i & 1) == 0 ? StarType.FAST : StarType.SLOW);
    }
  }

  private static int stir(final int seed, final int leftShift, final int rightShift,
                          final int secondLeftShift) {
    int value = seed & 32767;
    value = value ^ ((value << leftShift) & 32767);
    value = value ^ (value >> rightShift);
    value = value ^ ((value << secondLeftShift) & 32767);
    if (value == 0) {
      value = 1;
    }

    return value;
  }

  public void draw() {
    for (final Star star : this.stars) {
      star.draw();
    }
  }

  public void step() {
    int index = 0;
    while (index < COUNT) {
      this.shift(this.stars[index]);
      index = index + 1;
    }
  }

  private void plot(final Star star) {
    Screen.plot(star.x, star.y);
  }

  private void shift(final Star star) {
    star.move();
    star.draw();
  }
}
