"In ZX-Spectrum We Trust" :)


History
----------
I had some free time on the new year 2012 holidays and decided to write a small "spike" (in scrum therminology) to try JVM as a p-code source (like GWT for JavaScript) to be compiled into Z80 (8 bit) commands. I got working a small "Hello world" application and because it looked good and interestingly for me, I decided to implement its as one more my pet-project (of course I didn't want to implement entire JVM or make 64 bit operations on 8 bit processor :)) and as the result, the translator was born.


Reference
------------
The J2Z80 project is a translator of JVM byte codes into Z80 commands. It allows to translate classes situated in a JAR file into binary Z80-compatible representation which can be started on a Z80 based platform. It is absolutely free non-commercial project distributed under GPL v3 license.

3th part code usage
-----------------------
Because the project needs very strong testing on the low level, I actively use in my tests the open-source Z80 core written by E.Duijs for JEmu and improved by "mviara" (http://aud.freecode.com/users/mviara), many thanks to them both

Features
--------------
It supports : OOP, object creation, base arithmetic and bit operations, JNI, arrays
It doesn't support: some data types (long, float, double), exceptions, synchronization, any garbage collection, StringBuilder and StringBuffer (so it's impossible to make string operations) also all Core Java API excluded
Specific usage: char is 8bit width and String can't contain more than 255 symbols, int is 16 bit signed type. It uses small embedded Z80-assembler for translation.

but anyway the plugin opens a good way to use the most modern technologies in Z-80 development and in usage of the Java toolchain for it :)
at present I have not implemented any optimization in the generated code, so it looks not very optimally, may be I'll make it in the next versions.

Usage
------------
The translator is implemented as a maven plugin so you can use it just place the plugin reference into the build block
<plugin>
	<groupId>com.igormaznitsa.j2z80</groupId>
	<artifactId>J2Z80-Translator</artifactId>
	<version>0.1-SNAPSHOT</version>
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

0.1-SNAPSHOT (march 2012)
- the first published sources
- it contains some examples of usage and mainly working plugin
- it's not refactored well

0.0-SNAPSHOT (january 2012)
- only commands to show "Hello world" application was implemented
- only screenshots were published