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

- Case-insensitive Ant-style path patterns for resources to omit from the output. Resource paths
  are matched from the root, so patterns should start with `/`.
- Example:

```xml
<excludeResources>
  <value>/**/*.xml</value>
  <value>/**/*.properties</value>
  <value>/**/*.mf</value>
</excludeResources>
```

`optimization`

- Controls assembly peephole optimization.
- Allowed values: `NONE`, `BASIC`, `COMPACT`.
- Default: `NONE`.
- `BASIC` removes redundant stack cleanups such as `PUSH HL` / `POP HL` pairs.
- `COMPACT` includes `BASIC`, removes `JP`/`JR` branches whose destination is the immediately following label, and
  applies a few local size optimizations: `LD A,0` becomes `XOR A` when the next instruction overwrites all flags;
  `CP 0` is removed when the next instruction overwrites all flags; and `LD A,r` followed by `NEG` becomes `XOR A` /
  `SUB r` for `r` in `B`, `C`, `D`, `E`, `H`, or `L`, unless a label points to `NEG`. Each safe group of eight
  consecutive `ADD HL,HL` instructions is replaced with `LD H,L` / `LD L,0`, saving five bytes. Adjacent immediate
  loads into `B`/`C`, `D`/`E`, or `H`/`L` are combined into a single 16-bit load when both values are constants;
  redundant duplicate accumulator clears/tests are removed; repeated identical loads into `BC`, `DE`, `HL`, `IX`, or
  `IY` are removed when they only separate pushes of that same pair.
- The configured optimizers are rerun in order until a full pass makes no further changes.

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
- `BASIC` and `COMPACT` apply only semantics-preserving rewrites. The `LD A,0` replacement is
  limited to cases where the following instruction overwrites all flags, because `LD` preserves flags and `XOR` does
  not.
- The compare is removed only when the next instruction overwrites all flags. Replacing `CP 0` with `OR A` before a
  branch is not generally safe because `CP` sets overflow while `OR` sets parity.
- The `ADD HL,HL` sequence is only replaced when its final flags are overwritten, or when a later doubling in the
  same run recalculates the changed flags. A final group is retained otherwise. Runs shorter than eight doublings
  remain unchanged.
- The local substitutions in `COMPACT` follow idioms described in
  the [Wikiti Z80 optimization guide](https://wikiti.brandonw.net/index.php?title=Z80_Optimization).
  The [MDL Z80 optimizer](https://github.com/santiontanon/mdlz80optimizer) includes broader search- and flow-based
  passes that require more analysis than this local peephole optimizer performs.
- The plugin can resolve additional Z80-classified dependencies from the project so that translated code can reference
  platform or library resources.

## Example end-to-end build

```bash
mvn22 -pl j2z80-plugin -am install
mvn22 -pl j2z80-examples/zx-spectrum-hello-world -am install
```

After the second command, the example project will produce the generated Z80 outputs in its target directory ready for
loading in a ZX Spectrum emulator.
