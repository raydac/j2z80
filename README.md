Description
============
It is a maven plugin developed for academical purposes, the plugin allows to translate compiled JVM byte codes into Z80 instructions. It works as a pattern compiler with minimal optimization. **Warning! It is not a JVM interpreter because it generates low-level native code for Z80. It doesn't contain any GC!**

![Screenshot](http://www.igormaznitsa.com/projects/j2z80/j2z80scr1.jpg)

As the input it uses JAR files  It takes a JAR file and translate all found classes into solid Z80 binary block which can be started on real device or under emulator. 

It is not fully compatible with Java and has a lot of restrictions but it allows to use Java tool-chain and IDEs for Z80 developments.

License
========
The Sources published under [GNU GPL 3](http://www.gnu.org/copyleft/gpl.html)
