package com.igormaznitsa.test.helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import j2z80.spectrum.Screen;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToIntFunction;
import org.junit.Test;

public class StarFieldTest {

  private static int risingSteps(final Star[] stars, final Comparator<Star> order,
                                 final ToIntFunction<Star> axis) {
    Star[] ordered = Arrays.copyOf(stars, stars.length);
    Arrays.sort(ordered, order);
    int rising = 0;
    int index = 1;
    while (index < ordered.length) {
      if (axis.applyAsInt(ordered[index]) >= axis.applyAsInt(ordered[index - 1])) {
        rising = rising + 1;
      }
      index = index + 1;
    }
    return rising;
  }

  private static boolean onScreen(final Star star) {
    return star.x >= 0 && star.x < Screen.WIDTH
        && star.y >= 0 && star.y < Screen.HEIGHT
        && star.speed >= 1 && star.speed <= 4;
  }

  @Test
  public void moveWrapsPastTheRightEdge() {
    Star star = new Star(255, 40, 1);
    star.move();
    assertEquals(0, star.x);
    assertEquals(40, star.y);

    star.speed = 4;
    star.x = 253;
    star.move();
    assertEquals(1, star.x);
  }

  @Test
  public void moveKeepsAPointInsideTheScreen() {
    Star star = new Star(10, 20, 3);
    star.move();
    assertEquals(13, star.x);
    assertEquals(20, star.y);
  }

  @Test
  public void fieldScattersStarsAcrossTheScreen() {
    StarField sky = new StarField();
    assertEquals(StarField.COUNT, sky.stars.length);
    assertTrue(Arrays.stream(sky.stars).allMatch(StarFieldTest::onScreen));
    assertTrue(Arrays.stream(sky.stars).mapToInt(star -> star.x).distinct().count() > 140);
    assertTrue(Arrays.stream(sky.stars).mapToInt(star -> star.y).distinct().count() > 120);
    assertTrue(
        risingSteps(sky.stars, Comparator.comparingInt(star -> star.x), star -> star.y) < 180);
    assertTrue(
        risingSteps(sky.stars, Comparator.comparingInt(star -> star.y), star -> star.x) < 180);
  }
}
