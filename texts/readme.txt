History
----------
I had some free time on the new year 2012 holidays and decided to write a small "spike" (in scrum therminology) to try JVM as a p-code source (like GWT for JavaScript) to be compiled into Z80 (8 bit) commands. I got working a small "Hello world" application and because it looked good and interestingly for me, I decided to implement its as one more my pet-project (of course I didn't want to implement entire JVM or make 64 bit operations on 8 bit processor and as the result, the translator was born.

Reference
------------
The J2Z80 project is a translator of compiled Java classes into Z80 commands. It allows to translate classes (packed as a JAR file) into binary Z80-compatible representation which can be started on a Z80 based platform. It is absolutely free non-commercial project distributed under GPL v3 license.

3th part code usage
-----------------------
Because the project needs very strong testing on the low level, I actively use in my tests the open-source Z80 core written by E.Duijs for JEmu and improved by "mviara" (http://aud.freecode.com/users/mviara), many thanks to them both

Features of the translator
-----------------------------
It supports : OOP, object creation, base arithmetic and bit operations, JNI, arrays
It doesn't support: some data types (long, float, double), exceptions, synchronization, any garbage collection, both StringBuilder and StringBuffer (so it's impossible to make string operations) also all Core Java API excluded
Specific usage: char is 8bit width and String can't contain more than 255 symbols, int is 16 bit signed type. It uses small embedded Z80-assembler for translation.

but anyway the plugin opens a good way to use the most modern technologies for Z80 developments and allow to use the Java toolchain.
at present I have implemented obly very basic optimization in the generated code.

Usage
------------
The translator is implemented as a maven plugin so you can use it just place the plugin reference into the build block
<plugin>
	<groupId>com.igormaznitsa.j2z80</groupId>
	<artifactId>J2Z80-Translator</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<phase>install</phase>
			<goals>
				<goal>translate2z80</goal>    
			</goals>
		</execution>    
	</executions>    
</plugin>


Links
-----------------------------------------------------------
Author's email: Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
The Project home page: http://code.google.com/p/j2z80/


Versions
----------------------
1.0.1 (may 2012) hot fix
- removed the 'aux' word which was used as a package name, it has been changed to 'utils' because Windows doesn't support the folder name.

1.0.0 (may 2012)
- bug fixing
- commenting
- added base optimization
- minor improvements

0.2-SNAPSHOT (may 2012)
- added support to process JAR dependencies
- added support to include binary resources from JAR into compiled block as labeled byte arrays
- test improvements

0.1-SNAPSHOT (march 2012)
- the first published sources
- it contains some examples of usage and mainly working plugin
- it's not refactored well

0.0-SNAPSHOT (january 2012)
- only commands to show "Hello world" application was implemented
- only screenshots were published