package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.TABLESWITCH;
import org.junit.Test;

public class SwitchDispatchTest {

  @Test
  public void tableAndLookupSwitchesHonorBreakAndDefault() throws Exception {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.switchy.Dispatch")
        .file("demo/switchy/Dispatch.java", """
            package demo.switchy;

            public class Dispatch {
              public static int tableBreak;
              public static int tableFall;
              public static int tableNext;
              public static int tableHole;
              public static int tableTail;
              public static int tableDefault;
              public static int lookupNegative;
              public static int lookupZero;
              public static int lookupMiddle;
              public static int lookupHigh;
              public static int lookupDefault;
              public static int loopTotal;
            
              public static void mainz() {
                tableBreak = fromTable(1);
                tableFall = fromTable(2);
                tableNext = fromTable(3);
                tableHole = fromTable(5);
                tableTail = fromTable(6);
                tableDefault = fromTable(99);
                lookupNegative = fromLookup(-40);
                lookupZero = fromLookup(0);
                lookupMiddle = fromLookup(500);
                lookupHigh = fromLookup(9000);
                lookupDefault = fromLookup(4);
                loopTotal = accumulate();
              }
            
              private static int fromTable(final int code) {
                int value = 0;
                switch (code) {
                  case 1:
                    value = 10;
                    break;
                  case 2:
                    value = 20;
                  case 3:
                    value = value + 3;
                    break;
                  case 4:
                    value = 40;
                    break;
                  case 6:
                    value = 60;
                    break;
                  default:
                    value = -1;
                    break;
                }
                return value;
              }
            
              private static int fromLookup(final int code) {
                int value = 0;
                switch (code) {
                  case -40:
                    value = 7;
                    break;
                  case 0:
                    value = 8;
                    break;
                  case 500:
                    value = 9;
                    break;
                  case 9000:
                    value = 11;
                    break;
                  default:
                    value = 1;
                    break;
                }
                return value;
              }
            
              private static int accumulate() {
                int total = 0;
                for (int index = 0; index < 6; index++) {
                  switch (index) {
                    case 0:
                      total = total + 1;
                      break;
                    case 1:
                      total = total + 10;
                      break;
                    default:
                      total = total + 100;
                      break;
                  }
                }
                return total;
              }
            }
            """)
        .execute();

    final JavaClass compiled = new ClassParser(Path.of(
        "target", "z80-java", "classes", "demo", "switchy", "Dispatch.class").toString()).parse();
    assertTrue(containsSwitch(compiled, true));
    assertTrue(containsSwitch(compiled, false));

    assertEquals(10, run.staticInt("demo.switchy.Dispatch", "tableBreak"));
    assertEquals(23, run.staticInt("demo.switchy.Dispatch", "tableFall"));
    assertEquals(3, run.staticInt("demo.switchy.Dispatch", "tableNext"));
    assertEquals(-1, run.staticInt("demo.switchy.Dispatch", "tableHole"));
    assertEquals(60, run.staticInt("demo.switchy.Dispatch", "tableTail"));
    assertEquals(-1, run.staticInt("demo.switchy.Dispatch", "tableDefault"));
    assertEquals(7, run.staticInt("demo.switchy.Dispatch", "lookupNegative"));
    assertEquals(8, run.staticInt("demo.switchy.Dispatch", "lookupZero"));
    assertEquals(9, run.staticInt("demo.switchy.Dispatch", "lookupMiddle"));
    assertEquals(11, run.staticInt("demo.switchy.Dispatch", "lookupHigh"));
    assertEquals(1, run.staticInt("demo.switchy.Dispatch", "lookupDefault"));
    assertEquals(411, run.staticInt("demo.switchy.Dispatch", "loopTotal"));
  }

  @Test
  public void switchHandlesSignedIntegerExtremesAndCharValues() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.switchy.Boundaries")
        .file("demo/switchy/Boundaries.java", """
            package demo.switchy;
            
            public class Boundaries {
              public static int minimum;
              public static int maximum;
              public static int belowMinimum;
              public static int aboveMaximum;
              public static int charMinimum;
              public static int charBoundary;
            
              public static void mainz() {
                minimum = fromInt(-32767 - 1);
                maximum = fromInt(32767);
                belowMinimum = fromInt(-32767);
                aboveMaximum = fromInt(32766);
                charMinimum = fromChar((char) 0);
                charBoundary = fromChar((char) 255);
              }
            
              private static int fromInt(final int value) {
                switch (value) {
                  case -32768:
                    return 1;
                  case 32767:
                    return 2;
                  default:
                    return 9;
                }
              }
            
              private static int fromChar(final char value) {
                switch (value) {
                  case 0:
                    return 3;
                  case 255:
                    return 4;
                  default:
                    return 8;
                }
              }
            }
            """)
        .execute();

    assertEquals(1, run.staticInt("demo.switchy.Boundaries", "minimum"));
    assertEquals(2, run.staticInt("demo.switchy.Boundaries", "maximum"));
    assertEquals(9, run.staticInt("demo.switchy.Boundaries", "belowMinimum"));
    assertEquals(9, run.staticInt("demo.switchy.Boundaries", "aboveMaximum"));
    assertEquals(3, run.staticInt("demo.switchy.Boundaries", "charMinimum"));
    assertEquals(4, run.staticInt("demo.switchy.Boundaries", "charBoundary"));
  }

  private boolean containsSwitch(final JavaClass compiled, final boolean table) {
    for (final Method method : compiled.getMethods()) {
      if (method.getCode() == null) {
        continue;
      }
      final InstructionList list = new InstructionList(method.getCode().getCode());
      for (final InstructionHandle handle : list) {
        if (table && handle.getInstruction() instanceof TABLESWITCH) {
          return true;
        }
        if (!table && handle.getInstruction() instanceof LOOKUPSWITCH) {
          return true;
        }
      }
    }
    return false;
  }
}
