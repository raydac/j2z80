package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TryCatchFinallyTest {

  @Test
  public void nestedTryCatchFinallyRunsTheMatchingHandlers() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.ex.Guard")
        .file("demo/ex/Signal.java", """
            package demo.ex;

            public class Signal extends Exception {
            }
            """)
        .file("demo/ex/Bomb.java", """
            package demo.ex;

            public class Bomb extends Signal {
            }
            """)
        .file("demo/ex/Other.java", """
            package demo.ex;

            public class Other extends Signal {
            }
            """)
        .file("demo/ex/Guard.java", """
            package demo.ex;

            public class Guard {
              public static int normalFinally;
              public static int caught;
              public static int finallyOnThrow;
              public static int wrongCatch;
              public static int parentCatch;
              public static int parentFinally;
              public static int innerWrong;
              public static int innerFinally;
              public static int outerCatch;
              public static int outerFinally;
              public static int breakFinally;

              public static void mainz() throws Signal {
                try {
                  normalFinally = 1;
                } finally {
                  normalFinally = normalFinally + 10;
                }

                try {
                  throwSignal(new Bomb());
                } catch (Other other) {
                  wrongCatch = 9;
                } catch (Bomb bomb) {
                  caught = 3;
                } finally {
                  finallyOnThrow = finallyOnThrow + 5;
                }

                try {
                  throwSignal(new Bomb());
                } catch (Signal signal) {
                  parentCatch = 8;
                } finally {
                  parentFinally = parentFinally + 1;
                }

                try {
                  try {
                    boom();
                  } catch (Other other) {
                    innerWrong = 1;
                  } finally {
                    innerFinally = innerFinally + 1;
                  }
                } catch (Bomb bomb) {
                  outerCatch = 7;
                } finally {
                  outerFinally = outerFinally + 1;
                }

                int value = 0;
                for (int index = 0; index < 3; index++) {
                  try {
                    if (index == 1) {
                      break;
                    }
                    value = value + 1;
                  } finally {
                    value = value + 10;
                  }
                }
                breakFinally = value;
              }

              private static void throwSignal(final Signal signal) throws Signal {
                throw signal;
              }

              private static void boom() throws Signal {
                throw new Bomb();
              }
            }
            """)
        .execute();

    assertEquals(11, run.staticInt("demo.ex.Guard", "normalFinally"));
    assertEquals(0, run.staticInt("demo.ex.Guard", "wrongCatch"));
    assertEquals(3, run.staticInt("demo.ex.Guard", "caught"));
    assertEquals(5, run.staticInt("demo.ex.Guard", "finallyOnThrow"));
    assertEquals(8, run.staticInt("demo.ex.Guard", "parentCatch"));
    assertEquals(1, run.staticInt("demo.ex.Guard", "parentFinally"));
    assertEquals(0, run.staticInt("demo.ex.Guard", "innerWrong"));
    assertEquals(1, run.staticInt("demo.ex.Guard", "innerFinally"));
    assertEquals(7, run.staticInt("demo.ex.Guard", "outerCatch"));
    assertEquals(1, run.staticInt("demo.ex.Guard", "outerFinally"));
    assertEquals(21, run.staticInt("demo.ex.Guard", "breakFinally"));
  }
}
