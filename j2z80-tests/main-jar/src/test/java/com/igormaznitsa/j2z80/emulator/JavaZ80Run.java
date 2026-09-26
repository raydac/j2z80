package com.igormaznitsa.j2z80.emulator;

import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelNameForField;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.find;
import static java.nio.file.Files.write;
import static java.util.stream.Collectors.toList;

import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.bcel.generic.Type;

public final class JavaZ80Run {

  private static final int ORIGIN = 0x8000;
  private static final int STACK_TOP = 0xFF00;
  private static final int INSTRUCTION_LIMIT = 500_000;
  private static final String MAIN_LOOP = "___MAINLOOP___";

  private final String mainClassName;
  private final Map<String, String> sources = new LinkedHashMap<>();
  private Program program;
  private Z80Machine machine;

  private JavaZ80Run(final String mainClassName) {
    this.mainClassName = mainClassName;
  }

  public static JavaZ80Run mainClass(final String mainClassName) {
    return new JavaZ80Run(mainClassName);
  }

  public JavaZ80Run file(final String relativePath, final String source) {
    this.sources.put(relativePath, source);
    return this;
  }

  public JavaZ80Run execute() {
    try {
      final Path root = createDirectories(Path.of("target", "z80-java"));
      final Path sourcesDir = createDirectories(root.resolve("src"));
      final Path classesDir = createDirectories(root.resolve("classes"));
      final Path jar = root.resolve(this.mainClassName.replace('.', '-') + ".jar");
      this.writeSources(sourcesDir);
      this.compile(sourcesDir, classesDir);
      this.pack(classesDir, jar);

      final List<String> assembly =
          new TranslatorImpl(new SilentLogger(), OptimizationLevel.NONE, List.of(jar))
              .translate(this.mainClassName, ORIGIN, STACK_TOP, new String[0],
                  JavaZ80Run.class.getClassLoader());
      this.program = Program.assemble(assembly);
      this.machine = new Z80Machine();
      this.machine.load(this.program);
      this.machine.runUntil(this.program.origin(), this.program.addressOf(MAIN_LOOP),
          INSTRUCTION_LIMIT);
      return this;
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  public int staticInt(final String className, final String fieldName) {
    final int bits = this.machine.wordAt(this.program.addressOf(
        makeLabelNameForField(className, fieldName, Type.INT)));
    return (short) bits;
  }

  private void writeSources(final Path sourcesDir) throws IOException {
    for (final Map.Entry<String, String> source : this.sources.entrySet()) {
      final Path file = sourcesDir.resolve(source.getKey());
      createDirectories(file.getParent() == null ? sourcesDir : file.getParent());
      write(file, source.getValue().getBytes(UTF_8));
    }
  }

  private void compile(final Path sourcesDir, final Path classesDir) throws IOException {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("the running JDK does not provide javac");
    }
    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, Locale.ROOT,
        UTF_8)) {
      final Iterable<? extends JavaFileObject> units =
          files.getJavaFileObjectsFromPaths(this.sourceFiles(sourcesDir));
      final Boolean compiled = compiler.getTask(null, files, diagnostics,
          List.of("--release", "11", "-d", classesDir.toString()), null, units).call();
      if (!Boolean.TRUE.equals(compiled)) {
        throw new IllegalStateException("javac failed: " + diagnostics.getDiagnostics());
      }
    }
  }

  private List<Path> sourceFiles(final Path sourcesDir) {
    return this.sources.keySet().stream().map(sourcesDir::resolve).collect(toList());
  }

  private void pack(final Path classesDir, final Path jar) throws IOException {
    try (OutputStream output = java.nio.file.Files.newOutputStream(jar);
         JarOutputStream archive = new JarOutputStream(output);
         Stream<Path> classFiles = find(classesDir, Integer.MAX_VALUE,
             (path, attributes) -> path.toString().endsWith(".class"))) {
      for (final Path classFile : classFiles.collect(toList())) {
        archive.putNextEntry(
            new JarEntry(classesDir.relativize(classFile).toString().replace('\\', '/')));
        archive.write(java.nio.file.Files.readAllBytes(classFile));
        archive.closeEntry();
      }
    }
  }

  private static final class SilentLogger implements TranslatorLogger {
    @Override
    public void logInfo(final String message) {
    }

    @Override
    public void logWarning(final String message) {
    }

    @Override
    public void logDebug(final String message) {
    }

    @Override
    public void logError(final String message) {
    }
  }
}
