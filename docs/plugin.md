# J2Z80 Maven plugin

The j2z80 Maven plugin translates a compiled Java JAR into Z80 machine code and writes the result as assembler, raw
binary, or a ZX Spectrum snapshot.

Artifact coordinates:

```xml
<groupId>com.igormaznitsa</groupId>
<artifactId>j2z80-plugin</artifactId>
<version>2.0.0-SNAPSHOT</version>
```

Goal:

```text
translate
```

The goal is normally bound to the `install` phase, but you can bind it to any lifecycle phase you need.

## Add the plugin to a project

Add the plugin under `<build><plugins>` and configure the execution:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.igormaznitsa</groupId>
      <artifactId>j2z80-plugin</artifactId>
      <version>2.0.0-SNAPSHOT</version>

      <executions>
        <execution>
          <phase>install</phase>
          <goals>
            <goal>translate</goal>
          </goals>
          <configuration>
            <formats>
              <format>A80</format>
              <format>BIN</format>
              <format>SNA</format>
            </formats>
          </configuration>
        </execution>
      </executions>

      <configuration>
        <optimization>BASIC</optimization>
        <startAddress>28672</startAddress>
        <stackTop>65533</stackTop>
        <logAsmText>false</logAsmText>
        <excludeResources>
          <value>/**/*.xml</value>
          <value>/**/*.properties</value>
          <value>/**/*.mf</value>
        </excludeResources>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This example generates all three formats into the project build directory:

- `${project.build.directory}/${project.build.finalName}.a80`
- `${project.build.directory}/${project.build.finalName}.bin`
- `${project.build.directory}/${project.build.finalName}.sna`

## Build the plugin from source

From the project root, build the plugin and install it into your local Maven repository:

```bash
mvn22 -pl j2z80-plugin -am install
```

To build the example app that uses the plugin:

```bash
mvn22 -pl j2z80-examples/zx-spectrum-hello-world -am install
```

## Typical usage flow

The plugin reads the project JAR by default and translates the classes it contains. It also resolves any project
dependencies that are tagged with `classifier=z80` and uses them as additional input libraries.

Example project setup:

```xml
<dependencies>
  <dependency>
    <groupId>com.igormaznitsa</groupId>
    <artifactId>zx-spectrum-demo-lib</artifactId>
    <version>${project.version}</version>
    <classifier>z80</classifier>
  </dependency>
</dependencies>
```

Then the translate goal is called during the build and writes the target files.

## Goal and call semantics

The goal name is `translate`.

A minimal invocation is:

```bash
mvn install
```

When the plugin is bound to `install`, the goal runs automatically. You can also run it explicitly:

```bash
mvn com.igormaznitsa:j2z80-plugin:translate
```

This translates the configured jar and emits the selected output formats.

## Configuration parameters

`jarFile`

- Path to the JAR to translate.
- Default: `${project.build.directory}${file.separator}${project.build.finalName}.jar`
- Example:

```xml
<jarFile>${project.build.directory}/my-app.jar</jarFile>
```

`formats`

- Output formats to generate.
- Allowed values: `A80`, `BIN`, `SNA`.
- Default: `A80`.
- Example:

```xml
<formats>
  <format>A80</format>
  <format>BIN</format>
</formats>
```

`startAddress`

- Address where the translated code begins.
- Default: `28672`.
- For SNA snapshots, this must fall in the range `0x4000..0xFFFF`.

`stackTop`

- Stack top for the translated program.
- Default: `65533`.
- For SNA snapshots, the value must be in `0..0xFFFD`.

`logAsmText`

- If `true`, logs the generated assembler source line by line.
- Default: `false`.

`excludeResources`

- Resource globs to skip while embedding the output.
- Example:

```xml
<excludeResources>
  <value>/**/*.xml</value>
  <value>/**/*.properties</value>
  <value>/**/*.mf</value>
</excludeResources>
```

`optimization`

- Controls stack-pair cleanup optimization.
- Allowed values: `NONE`, `BASIC`.
- Default: `NONE`.
- `BASIC` removes redundant stack cleanups such as `PUSH HL` / `POP HL` pairs.

## Output format notes

`A80`

- Writes the generated Z80 assembler source.

`BIN`

- Writes the raw translated machine code as a binary file.

`SNA`

- Writes a ZX Spectrum 48K snapshot containing the generated program image and execution state.

## Notes and constraints

- The plugin operates on the project build artifact by default, but you can point it at any JAR through `jarFile`.
- Output files are written under the project build directory.
- There is no plugin parameter for the heap start. When the memory manager is needed, it is emitted after the translated
  code and embedded data; the initial heap pointer is set to the first address after the generated image. `startAddress`
  and `stackTop` do not independently set the heap start.
- When `optimization` is set to `BASIC`, the translator removes obvious redundant stack pairs but does not change the
  semantics of the program.
- The plugin can resolve additional Z80-classified dependencies from the project so that translated code can reference
  platform or library resources.

## Example end-to-end build

```bash
mvn22 -pl j2z80-plugin -am install
mvn22 -pl j2z80-examples/zx-spectrum-hello-world -am install
```

After the second command, the example project will produce the generated Z80 outputs in its target directory ready for
loading in a ZX Spectrum emulator.
