package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.UncheckedIOException;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class EnumRunTest {

  private static void assertStringEnumUseRejected(final ThrowingRunnable action,
                                                  final String method) {
    Throwable current = assertThrows(UncheckedIOException.class, action);
    while (current != null && !(current instanceof IllegalArgumentException)) {
      current = current.getCause();
    }
    assertTrue(
        current instanceof IllegalArgumentException && current.getMessage().contains(method));
  }

  @Test
  public void enumConstantsOrdinalValuesFieldsAndSwitchRunOnZ80() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.EnumUse")
        .file("demo/Color.java", """
            package demo;
            
            public enum Color {
              RED, GREEN, BLUE
            }
            """)
        .file("demo/Planet.java", """
            package demo;
            
            public enum Planet {
              EARTH(9), MARS(3);
            
              private final int mass;
            
              Planet(final int mass) {
                this.mass = mass;
              }
            
              public int mass() {
                return this.mass;
              }
            }
            """)
        .file("demo/Op.java", """
            package demo;
            
            public enum Op {
              ADD {
                public int apply(final int left, final int right) {
                  return left + right;
                }
              },
              SUB {
                public int apply(final int left, final int right) {
                  return left - right;
                }
              };
            
              public abstract int apply(int left, int right);
            }
            """)
        .file("demo/EnumUse.java", """
            package demo;
            
            public class EnumUse {
              public static int red;
              public static int green;
              public static int length;
              public static int blue;
              public static int copyKeepsRed;
              public static int earthMass;
              public static int marsOrdinal;
              public static int add;
              public static int sub;
              public static int selected;
            
              public static void mainz() {
                red = Color.RED.ordinal();
                green = Color.GREEN.ordinal();
            
                final Color[] first = Color.values();
                length = first.length;
                blue = first[2] == Color.BLUE ? 1 : 0;
                first[0] = null;
                copyKeepsRed = Color.values()[0] == Color.RED ? 1 : 0;
            
                earthMass = Planet.EARTH.mass();
                marsOrdinal = Planet.MARS.ordinal();
            
                add = Op.ADD.apply(20, 22);
                sub = Op.SUB.apply(20, 22);
            
                switch (Color.GREEN) {
                  case RED:
                    selected = 1;
                    break;
                  case GREEN:
                    selected = 2;
                    break;
                  default:
                    selected = 9;
                    break;
                }
              }
            }
            """)
        .execute();

    assertEquals(0, run.staticInt("demo.EnumUse", "red"));
    assertEquals(1, run.staticInt("demo.EnumUse", "green"));
    assertEquals(3, run.staticInt("demo.EnumUse", "length"));
    assertEquals(1, run.staticInt("demo.EnumUse", "blue"));
    assertEquals(1, run.staticInt("demo.EnumUse", "copyKeepsRed"));
    assertEquals(9, run.staticInt("demo.EnumUse", "earthMass"));
    assertEquals(1, run.staticInt("demo.EnumUse", "marsOrdinal"));
    assertEquals(42, run.staticInt("demo.EnumUse", "add"));
    assertEquals(-2, run.staticInt("demo.EnumUse", "sub"));
    assertEquals(2, run.staticInt("demo.EnumUse", "selected"));
  }

  @Test
  public void enumValueOfIsRejected() {
    assertStringEnumUseRejected(() -> {
      JavaZ80Run.mainClass("demo.EnumValueOf")
          .file("demo/Color.java", """
              package demo;
              
              public enum Color {
                RED, GREEN, BLUE
              }
              """)
          .file("demo/EnumValueOf.java", """
              package demo;
              
              public class EnumValueOf {
                public static int found;
              
                public static void mainz() {
                  found = Color.valueOf("RED") == Color.RED ? 1 : 0;
                }
              }
              """)
          .execute();
    }, "valueOf");
  }

  @Test
  public void enumNameIsRejected() {
    assertStringEnumUseRejected(() -> {
      JavaZ80Run.mainClass("demo.EnumName")
          .file("demo/Color.java", """
              package demo;
              
              public enum Color {
                RED, GREEN, BLUE
              }
              """)
          .file("demo/EnumName.java", """
              package demo;
              
              public class EnumName {
                public static int named;
              
                public static void mainz() {
                  named = Color.RED.name() == null ? 1 : 0;
                }
              }
              """)
          .execute();
    }, "name");
  }
}
