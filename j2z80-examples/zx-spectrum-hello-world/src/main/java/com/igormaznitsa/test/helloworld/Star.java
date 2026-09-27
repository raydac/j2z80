package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class Star {

  public int x;
  public int y;
  public int speed;

  public Star(final int x, final int y, final int speed) {
    this.x = x;
    this.y = y;
    this.speed = speed;
  }

  public void move() {
    int next = this.x + this.speed;
    if (next >= Screen.WIDTH) {
      next = next - Screen.WIDTH;
    }

    this.x = next;
  }
}
