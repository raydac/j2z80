Description
============
It is a maven plugin developed for academical purposes, the plugin allows to translate compiled JVM byte codes into Z80 instructions. It works as a pattern compiler with minimal optimization. **Warning! It is not a JVM interpreter because it generates low-level native code for Z80. It doesn't contain any GC!**

![Screenshot](https://raw.githubusercontent.com/raydac/j2z80/master/docs/java_on_spec.png)
```Java
package com.igormaznitsa.test.helloworld;

import static com.igormaznitsa.test.helloworld.ZXScreen.*;

public class main {
    public static final void mainz(){
        setTextColor(COLOR_RED, COLOR_WHITE);
        clearWholeScreen();
        System.out.println("Hello world!");
        setTextColor(COLOR_YELLOW, COLOR_BLUE);
        clearServiceScreen();
        System.err.println("Written in Java!!!");
        setBorderColor(COLOR_RED);
    }
}
```

As the input it uses JAR files  It takes a JAR file and translate all found classes into solid Z80 binary block which can be started on real device or under emulator. 

It is not fully compatible with Java and has a lot of restrictions but it allows to use Java tool-chain and IDEs for Z80 developments.

License
========
The Sources published under [GNU GPL 3](http://www.gnu.org/copyleft/gpl.html)
