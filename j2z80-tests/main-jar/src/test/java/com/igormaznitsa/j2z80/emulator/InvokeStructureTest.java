package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InvokeStructureTest {

  @Test
  public void classHierarchyUsesEveryInvokeKind() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.invoke.Tally")
        .file("demo/invoke/Meter.java", """
            package demo.invoke;

            public interface Meter {
              int measure(int seed);
            }
            """)
        .file("demo/invoke/Reading.java", """
            package demo.invoke;

            public class Reading {
              protected int bias;

              public Reading(final int bias) {
                this.bias = bias;
              }

              public int measure(final int seed) {
                return seed + this.bias;
              }

              public static int offset() {
                return 5;
              }
            }
            """)
        .file("demo/invoke/ScaledMeter.java", """
            package demo.invoke;

            public class ScaledMeter extends Reading implements Meter {
              public ScaledMeter(final int bias) {
                super(bias);
              }

              @Override
              public int measure(final int seed) {
                return this.scale(super.measure(seed));
              }

              public int direct() {
                return super.measure(1) + this.scale(2);
              }

              private int scale(final int value) {
                return value + Reading.offset();
              }
            }
            """)
        .file("demo/invoke/AltMeter.java", """
            package demo.invoke;

            public class AltMeter implements Meter {
              @Override
              public int measure(final int seed) {
                return seed + 1;
              }
            }
            """)
        .file("demo/invoke/Tally.java", """
            package demo.invoke;

            public class Tally {
              public static int staticResult;
              public static int specialResult;
              public static int virtualResult;
              public static int interfaceResult;

              public static void mainz() {
                staticResult = Reading.offset();

                final ScaledMeter scaled = new ScaledMeter(10);
                specialResult = scaled.direct();

                final Reading asReading = scaled;
                virtualResult = asReading.measure(7);

                final Meter asMeter = scaled;
                final Meter other = new AltMeter();
                interfaceResult = asMeter.measure(7) + other.measure(7);
              }
            }
            """)
        .execute();

    assertEquals(5, run.staticInt("demo.invoke.Tally", "staticResult"));
    assertEquals(18, run.staticInt("demo.invoke.Tally", "specialResult"));
    assertEquals(22, run.staticInt("demo.invoke.Tally", "virtualResult"));
    assertEquals(30, run.staticInt("demo.invoke.Tally", "interfaceResult"));
  }
}
