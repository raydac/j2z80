package com.igormaznitsa.test.helloworld;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CurvesTest {

  @Test
  public void halfWaveIsNearZeroAtTheOrigin() {
    assertTrue(Curves.halfWave(0.02f) < 0.05f);
  }

  @Test
  public void halfWavePeaksNearOne() {
    float peak = Curves.halfWave(1.5708f);
    assertTrue(peak > 0.95f && peak < 1.05f);
  }

  @Test
  public void doubleTurnKeepsTheRadius() {
    double x = Curves.turnedX(1.0, 0.0, 0.965925826, 0.258819045);
    double y = Curves.turnedY(1.0, 0.0, 0.965925826, 0.258819045);
    double radius = x * x + y * y;
    assertTrue(radius > 0.98 && radius < 1.02);
  }
}
