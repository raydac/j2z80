[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Java 22+](https://img.shields.io/badge/java-22%2b-green.svg)](https://bell-sw.com/pages/downloads/#jdk-25-lts)   
[![Arthur's Acres Animal Sanctuary — donate](docs/arthur_sanctuary_banner.png)](https://www.arthursacresanimalsanctuary.org/donate)

Description
============
It is a maven plugin developed for academical purposes, the plugin allows to translate compiled JVM byte codes into Z80 instructions. It works as a pattern compiler with minimal optimization. **Warning! It is not a JVM interpreter because it generates low-level native code for Z80. It doesn't contain any GC!**

![Screenshot](docs/j2z80_hello_world.gif)

```Java
package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class main {
  public static void mainz() {
        Screen.paper(Screen.WHITE);
        Screen.ink(Screen.BLACK);
        Screen.clear();
        Screen.border(Screen.GREEN);
        Screen.at(10, 8);
        System.out.println("Hello world 2026");
        Screen.lowerColors(Screen.YELLOW, Screen.BLUE, 1, 0);
        Screen.clearLower();
        System.err.println("SPACE draws the set");
    }
}
```

The same example then waits for space and draws a Mandelbrot set on the attribute grid, followed by a closer view of the
seahorse valley.

As the input it uses JAR files  It takes a JAR file and translate all found classes into solid Z80 binary block which can be started on real device or under emulator. 

It is not fully compatible with Java and has a lot of restrictions but it allows to use Java tool-chain and IDEs for Z80 developments.
