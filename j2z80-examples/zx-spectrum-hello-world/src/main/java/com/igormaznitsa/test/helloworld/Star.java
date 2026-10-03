package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class Star {

  public int x;
  public int y;
  public int speed;

  public final StarType type;

  public Star(final int x, final int y, final int speed, final StarType type) {
    this.x = x;
    this.y = y;
    this.type = type;
    this.speed = speed;
  }

  public void move() {
    Screen.unplot(this.x, this.y);
    int next = this.x + (type == StarType.SLOW ? this.speed : this.speed << 1);
    if (next >= Screen.WIDTH) {
      next = next - Screen.WIDTH;
    }

    this.x = next;
  }

  public void draw() {
    final int starColor;
    switch (this.type) {
      case SLOW: {
        starColor = Screen.RED;
      }
      break;
      case FAST: {
        starColor = Screen.GREEN;
      }
      break;
      default: {
        starColor = Screen.YELLOW;
      }
      break;
    }

    Screen.plot(this.x, this.y, starColor);
  }
}
