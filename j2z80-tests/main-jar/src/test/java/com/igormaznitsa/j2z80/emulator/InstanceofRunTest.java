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

  @Test
  public void instanceofRecognizesInheritedAndUnrelatedInterfaces() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.InterfaceInstanceof")
        .file("demo/Marker.java", """
            package demo;
            
            public interface Marker {
            }
            """)
        .file("demo/ExtendedMarker.java", """
            package demo;
            
            public interface ExtendedMarker extends Marker {
            }
            """)
        .file("demo/OtherMarker.java", """
            package demo;
            
            public interface OtherMarker {
            }
            """)
        .file("demo/MarkedBase.java", """
            package demo;
            
            public class MarkedBase implements ExtendedMarker {
            }
            """)
        .file("demo/MarkedChild.java", """
            package demo;
            
            public class MarkedChild extends MarkedBase {
            }
            """)
        .file("demo/Unmarked.java", """
            package demo;
            
            public class Unmarked implements OtherMarker {
            }
            """)
        .file("demo/InterfaceInstanceof.java", """
            package demo;
            
            public class InterfaceInstanceof {
              public static int directParentInterface;
              public static int inheritedInterface;
              public static int unrelatedInterface;
              public static int nullInterface;
            
              public static void mainz() {
                final Object direct = new MarkedBase();
                final Object child = new MarkedChild();
                final Object other = new Unmarked();
                final Object missing = null;
            
                directParentInterface = direct instanceof Marker ? 1 : 0;
                inheritedInterface = child instanceof ExtendedMarker ? 1 : 0;
                unrelatedInterface = other instanceof Marker ? 1 : 0;
                nullInterface = missing instanceof Marker ? 1 : 0;
              }
            }
            """)
        .execute();

    assertEquals(1, run.staticInt("demo.InterfaceInstanceof", "directParentInterface"));
    assertEquals(1, run.staticInt("demo.InterfaceInstanceof", "inheritedInterface"));
    assertEquals(0, run.staticInt("demo.InterfaceInstanceof", "unrelatedInterface"));
    assertEquals(0, run.staticInt("demo.InterfaceInstanceof", "nullInterface"));
  }

  @Test
  public void instanceofRecognizesReferenceArraysAsObjects() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.ArrayInstanceof")
        .file("demo/ArrayInstanceof.java", """
            package demo;
            
            public class ArrayInstanceof {
              public static int referenceArrayIsObject;
              public static int nestedArrayIsObject;
              public static int primitiveArrayIsObject;
              public static int nullIsNotObject;
            
              public static void mainz() {
                final Object references = new String[1];
                final Object nested = new int[1][];
                final Object primitives = new int[1];
            
                referenceArrayIsObject = references instanceof Object ? 1 : 0;
                nestedArrayIsObject = nested instanceof Object ? 1 : 0;
                primitiveArrayIsObject = primitives instanceof Object ? 1 : 0;
                nullIsNotObject = ((Object) null) instanceof Object ? 1 : 0;
              }
            }
            """)
        .execute();

    assertEquals(1, run.staticInt("demo.ArrayInstanceof", "referenceArrayIsObject"));
    assertEquals(1, run.staticInt("demo.ArrayInstanceof", "nestedArrayIsObject"));
    assertEquals(1, run.staticInt("demo.ArrayInstanceof", "primitiveArrayIsObject"));
    assertEquals(0, run.staticInt("demo.ArrayInstanceof", "nullIsNotObject"));
  }
}
