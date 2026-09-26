package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JavaZ80RunTest {

  @Test
  public void compiledAdditionMatchesTheJavaResult() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.Add")
        .file("demo/Add.java", """
            package demo;

            public class Add {
              public static int result;

              public static void mainz() {
                result = 20 + 22;
              }
            }
            """)
        .execute();

    assertEquals(42, run.staticInt("demo.Add", "result"));
  }

  @Test
  public void compiledLoopAndSecondClassMatchTheJavaResult() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.Measure")
        .file("demo/Pair.java", """
            package demo;

            public class Pair {
              public static int left;
              public static int right;

              public static int gap() {
                return right - left;
              }
            }
            """)
        .file("demo/Measure.java", """
            package demo;

            public class Measure {
              public static int result;

              public static void mainz() {
                Pair.left = 8;
                Pair.right = 30;
                result = scale(sumThrough(10)) + Pair.gap();
              }

              private static int sumThrough(final int limit) {
                int total = 0;
                int index = 1;
                while (index <= limit) {
                  total = total + index;
                  index = index + 1;
                }
                return total;
              }

              private static int scale(final int value) {
                if (value > 50) {
                  return value + value;
                }
                return value;
              }
            }
            """)
        .execute();

    assertEquals(132, run.staticInt("demo.Measure", "result"));
  }

  @Test
  public void forLoopContinueSkipsTheBodyAndBreakLeaves() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.ForLoop")
        .file("demo/ForLoop.java", """
            package demo;

            public class ForLoop {
              public static int result;

              public static void mainz() {
                int total = 0;
                for (int index = 1; index <= 12; index++) {
                  if ((index & 1) == 0) {
                    continue;
                  }
                  if (index > 9) {
                    break;
                  }
                  total = total + index;
                }
                result = total;
              }
            }
            """)
        .execute();

    assertEquals(25, run.staticInt("demo.ForLoop", "result"));
  }

  @Test
  public void whileLoopContinueAndBreakSelectTheMiddleValues() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.WhileLoop")
        .file("demo/WhileLoop.java", """
            package demo;

            public class WhileLoop {
              public static int result;

              public static void mainz() {
                int total = 0;
                int index = 0;
                while (true) {
                  index = index + 1;
                  if (index < 4) {
                    continue;
                  }
                  if (index == 8) {
                    break;
                  }
                  total = total + index;
                }
                result = total;
              }
            }
            """)
        .execute();

    assertEquals(22, run.staticInt("demo.WhileLoop", "result"));
  }

  @Test
  public void nestedLoopsHonorInnerBreakAndOuterContinue() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.NestedLoops")
        .file("demo/NestedLoops.java", """
            package demo;

            public class NestedLoops {
              public static int result;

              public static void mainz() {
                int total = 0;
                for (int row = 1; row <= 4; row++) {
                  if (row == 2) {
                    continue;
                  }
                  int column = 1;
                  while (column <= 5) {
                    if (column == 3) {
                      column = column + 1;
                      continue;
                    }
                    if (column == 5) {
                      break;
                    }
                    total = total + row * 10 + column;
                    column = column + 1;
                  }
                }
                result = total;
              }
            }
            """)
        .execute();

    assertEquals(261, run.staticInt("demo.NestedLoops", "result"));
  }

  @Test
  public void labeledBreakAndContinueLeaveTheOuterLoop() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.LabeledLoops")
        .file("demo/LabeledLoops.java", """
            package demo;

            public class LabeledLoops {
              public static int result;

              public static void mainz() {
                int total = 0;
                outer:
                for (int row = 1; row <= 6; row++) {
                  for (int column = 1; column <= 6; column++) {
                    if (column == 2) {
                      continue outer;
                    }
                    if (row == 4) {
                      break outer;
                    }
                    total = total + row * 10 + column;
                  }
                }
                result = total;
              }
            }
            """)
        .execute();

    assertEquals(63, run.staticInt("demo.LabeledLoops", "result"));
  }
}
