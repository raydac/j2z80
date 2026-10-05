package com.igormaznitsa.j2z80.emulator;

import static com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager.MEMORY_HEAP_START_AREA_LABEL;
import static com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager.VAR_MANAGER_TOP_POINTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ForgetHeapTest {

  private static final int EXTRA_OBJECTS = 100;
  private static final int OBJECT_HEADER_BYTES = 4;

  private static int address(final int signedWord) {
    return signedWord & 0xFFFF;
  }

  @Test
  public void forgettingAnInstanceRewindsTheHeapPastLaterObjects() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.ForgetHeap")
        .withRuntimeClasspath()
        .file("demo/Cell.java", """
            package demo;
            
            public class Cell {
              public int value;
            }
            """)
        .file("demo/ForgetHeap.java", """
            package demo;
            
            import j2z80.Heap;
            
            public class ForgetHeap {
              public static int anchorAddress;
              public static int middleAddress;
              public static int lastAddress;
              public static int reusedAddress;
              public static int anchorValue;
              public static int created;
              public static int topAtStart;
              public static int topAfter;
              public static int topRewound;
            
              public static void mainz() {
                topAtStart = Heap.top();
            
                final Cell anchor = new Cell();
                anchor.value = 11;
                anchorAddress = anchor.hashCode();
            
                final Cell middle = new Cell();
                middle.value = 22;
                middleAddress = middle.hashCode();
            
                int index = 0;
                Cell last = middle;
                while (index < %d) {
                  last = new Cell();
                  last.value = index + 1;
                  index = index + 1;
                }
                created = index;
                lastAddress = last.hashCode();
                topAfter = Heap.top();
            
                Heap.forget(middle);
            
                final Cell reused = new Cell();
                reused.value = 22;
                reusedAddress = reused.hashCode();
                anchorValue = anchor.value;
            
                Heap.forget(anchor);
                topRewound = Heap.top();
              }
            }
            """.formatted(EXTRA_OBJECTS))
        .execute();

    final int heapStart = run.addressOf(MEMORY_HEAP_START_AREA_LABEL);
    final int anchor = address(run.staticInt("demo.ForgetHeap", "anchorAddress"));
    final int middle = address(run.staticInt("demo.ForgetHeap", "middleAddress"));
    final int last = address(run.staticInt("demo.ForgetHeap", "lastAddress"));
    final int reused = address(run.staticInt("demo.ForgetHeap", "reusedAddress"));
    final int stride = middle - anchor;

    assertEquals(run.generatedImageEndAddress(), heapStart);
    assertEquals(EXTRA_OBJECTS, run.staticInt("demo.ForgetHeap", "created"));
    assertEquals(heapStart + OBJECT_HEADER_BYTES, anchor);
    assertTrue(stride > OBJECT_HEADER_BYTES);
    assertEquals((int) ((middle + (long) EXTRA_OBJECTS * stride) & 0xFFFF), last);
    assertEquals(middle, reused);
    assertNotEquals(anchor, reused);
    assertEquals(11, run.staticInt("demo.ForgetHeap", "anchorValue"));
    assertEquals(heapStart, run.wordAt(VAR_MANAGER_TOP_POINTER));
    assertEquals(heapStart, address(run.staticInt("demo.ForgetHeap", "topAtStart")));
    assertTrue(address(run.staticInt("demo.ForgetHeap", "topAfter")) > heapStart);
    assertEquals(heapStart, address(run.staticInt("demo.ForgetHeap", "topRewound")));
  }
}
