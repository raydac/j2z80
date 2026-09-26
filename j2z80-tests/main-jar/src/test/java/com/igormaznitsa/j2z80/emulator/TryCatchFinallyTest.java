package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TryCatchFinallyTest {

  @Test
  public void finallyRunsOnNormalExitFromDifferentCallSites() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.ex.FinallyOnly")
        .file("demo/ex/FinallyOnly.java", """
            package demo.ex;

            public class FinallyOnly {
              public static int fallThrough;
              public static int earlyReturn;
              public static int earlyReturnSide;
              public static int loopContinue;
              public static int loopBreak;
              public static int nestedInner;
              public static int nestedOuter;
              public static int calleeA;
              public static int calleeB;
              public static int calleeC;
              public static int sequential;

              public static void mainz() {
                fallThrough = withFallThrough(2);
                earlyReturn = withEarlyReturn(7);
                earlyReturnSide = earlyReturnSide;
                loopContinue = withContinue(4);
                loopBreak = withBreak(5);
                nestedInner = 0;
                nestedOuter = 0;
                withNested(3);
                calleeA = withCallee(1);
                calleeB = withCallee(2);
                calleeC = withCallee(3);
                sequential = withSequential();
              }

              private static int withFallThrough(final int seed) {
                int value = seed;
                try {
                  value = value + 1;
                } finally {
                  value = value + 10;
                }
                return value;
              }

              private static int withEarlyReturn(final int seed) {
                try {
                  earlyReturnSide = earlyReturnSide + 1;
                  return seed + 100;
                } finally {
                  earlyReturnSide = earlyReturnSide + 10;
                }
              }

              private static int withContinue(final int limit) {
                int value = 0;
                for (int index = 0; index < limit; index++) {
                  try {
                    if ((index & 1) == 0) {
                      continue;
                    }
                    value = value + 1;
                  } finally {
                    value = value + 10;
                  }
                }
                return value;
              }

              private static int withBreak(final int limit) {
                int value = 0;
                for (int index = 0; index < limit; index++) {
                  try {
                    if (index == 2) {
                      break;
                    }
                    value = value + 1;
                  } finally {
                    value = value + 10;
                  }
                }
                return value;
              }

              private static void withNested(final int seed) {
                try {
                  try {
                    nestedInner = seed;
                  } finally {
                    nestedInner = nestedInner + 10;
                  }
                  nestedOuter = nestedInner;
                } finally {
                  nestedOuter = nestedOuter + 100;
                }
              }

              private static int withCallee(final int seed) {
                int value = seed;
                try {
                  value = bump(value);
                } finally {
                  value = value + 20;
                }
                return value;
              }

              private static int bump(final int value) {
                return value + 3;
              }

              private static int withSequential() {
                int value = 0;
                try {
                  value = value + 1;
                } finally {
                  value = value + 10;
                }
                try {
                  value = value + 2;
                } finally {
                  value = value + 20;
                }
                try {
                  value = value + 3;
                } finally {
                  value = value + 30;
                }
                return value;
              }
            }
            """)
        .execute();

    assertEquals(13, run.staticInt("demo.ex.FinallyOnly", "fallThrough"));
    assertEquals(107, run.staticInt("demo.ex.FinallyOnly", "earlyReturn"));
    assertEquals(11, run.staticInt("demo.ex.FinallyOnly", "earlyReturnSide"));
    assertEquals(42, run.staticInt("demo.ex.FinallyOnly", "loopContinue"));
    assertEquals(32, run.staticInt("demo.ex.FinallyOnly", "loopBreak"));
    assertEquals(13, run.staticInt("demo.ex.FinallyOnly", "nestedInner"));
    assertEquals(113, run.staticInt("demo.ex.FinallyOnly", "nestedOuter"));
    assertEquals(24, run.staticInt("demo.ex.FinallyOnly", "calleeA"));
    assertEquals(25, run.staticInt("demo.ex.FinallyOnly", "calleeB"));
    assertEquals(26, run.staticInt("demo.ex.FinallyOnly", "calleeC"));
    assertEquals(66, run.staticInt("demo.ex.FinallyOnly", "sequential"));
  }

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
