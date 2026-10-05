package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InstanceofRunTest {

  @Test
  public void instanceofHandlesNullDirectChildAndUnrelatedInstances() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.InstanceofCheck")
        .file("demo/Parent.java", """
            package demo;
            
            public class Parent {
            }
            """)
        .file("demo/Child.java", """
            package demo;
            
            public class Child extends Parent {
            }
            """)
        .file("demo/Other.java", """
            package demo;
            
            public class Other {
            }
            """)
        .file("demo/InstanceofCheck.java", """
            package demo;
            
            public class InstanceofCheck {
              public static int nullReference;
              public static int directInstance;
              public static int childInstance;
              public static int unrelatedInstance;
            
              public static void mainz() {
                final Object missing = null;
                final Object direct = new Parent();
                final Object child = new Child();
                final Object unrelated = new Other();
            
                nullReference = missing instanceof Parent ? 1 : 0;
                directInstance = direct instanceof Parent ? 1 : 0;
                childInstance = child instanceof Parent ? 1 : 0;
                unrelatedInstance = unrelated instanceof Parent ? 1 : 0;
              }
            }
            """)
        .execute();

    assertEquals(0, run.staticInt("demo.InstanceofCheck", "nullReference"));
    assertEquals(1, run.staticInt("demo.InstanceofCheck", "directInstance"));
    assertEquals(1, run.staticInt("demo.InstanceofCheck", "childInstance"));
    assertEquals(0, run.staticInt("demo.InstanceofCheck", "unrelatedInstance"));
  }
}
