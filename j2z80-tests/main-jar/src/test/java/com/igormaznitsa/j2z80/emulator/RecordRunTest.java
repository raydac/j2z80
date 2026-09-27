package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecordRunTest {

  @Test
  public void recordAccessorsEqualsAndHashCodeRunOnZ80() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.RecordUse")
        .languageRelease(17)
        .file("demo/Point.java", """
            package demo;
            
            public record Point(int x, int y) {
              public int sum() {
                return this.x + this.y;
              }
            }
            """)
        .file("demo/Node.java", """
            package demo;
            
            public record Node(int value, Node next) {
            }
            """)
        .file("demo/Span.java", """
            package demo;
            
            public record Span(long width) {
            }
            """)
        .file("demo/RecordUse.java", """
            package demo;
            
            public class RecordUse {
              public static int x;
              public static int y;
              public static int sum;
              public static int same;
              public static int different;
              public static int nullEquals;
              public static int hashSame;
              public static int hashDiffers;
              public static int nestedSame;
              public static int nestedDifferent;
              public static int longSame;
              public static int longDifferent;
              public static int toStringNull;
            
              public static void mainz() {
                final Point left = new Point(3, 4);
                final Point right = new Point(3, 4);
                final Point other = new Point(3, 5);
                x = left.x();
                y = left.y();
                sum = left.sum();
                same = left.equals(right) ? 1 : 0;
                different = left.equals(other) ? 1 : 0;
                nullEquals = left.equals(null) ? 1 : 0;
                hashSame = left.hashCode() == right.hashCode() ? 1 : 0;
                hashDiffers = left.hashCode() == other.hashCode() ? 0 : 1;
                toStringNull = left.toString() == null ? 1 : 0;
            
                final Node chain = new Node(1, new Node(2, null));
                final Node copy = new Node(1, new Node(2, null));
                final Node changed = new Node(1, new Node(9, null));
                nestedSame = chain.equals(copy) ? 1 : 0;
                nestedDifferent = chain.equals(changed) ? 1 : 0;
            
                final Span wide = new Span(3L);
                longSame = wide.equals(new Span(3L)) ? 1 : 0;
                longDifferent = wide.equals(new Span(4L)) ? 1 : 0;
              }
            }
            """)
        .execute();

    assertEquals(3, run.staticInt("demo.RecordUse", "x"));
    assertEquals(4, run.staticInt("demo.RecordUse", "y"));
    assertEquals(7, run.staticInt("demo.RecordUse", "sum"));
    assertEquals(1, run.staticInt("demo.RecordUse", "same"));
    assertEquals(0, run.staticInt("demo.RecordUse", "different"));
    assertEquals(0, run.staticInt("demo.RecordUse", "nullEquals"));
    assertEquals(1, run.staticInt("demo.RecordUse", "hashSame"));
    assertEquals(1, run.staticInt("demo.RecordUse", "hashDiffers"));
    assertEquals(1, run.staticInt("demo.RecordUse", "nestedSame"));
    assertEquals(0, run.staticInt("demo.RecordUse", "nestedDifferent"));
    assertEquals(1, run.staticInt("demo.RecordUse", "longSame"));
    assertEquals(0, run.staticInt("demo.RecordUse", "longDifferent"));
    assertEquals(1, run.staticInt("demo.RecordUse", "toStringNull"));
  }
}
