/*
 * Copyright 2012-2026 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.j2z80.translator.mojos;

import static java.util.stream.Stream.concat;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.translator.Format;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.j2z80.translator.utils.JarClassLoaderFactory;
import com.igormaznitsa.j2z80.translator.utils.Sna48Writer;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;

/**
 * Translates a built Java archive into Z80 assembly and binary output during a Maven build.
 *
 * <p>The mojo resolves any project dependencies that provide Z80 classes, loads the generated
 * JAR, translates the bytecode, and writes the requested output formats to the build directory.
 */
@SuppressWarnings("unused")
@Mojo(name = "translate",
    defaultPhase = LifecyclePhase.PACKAGE,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TranslatorMojo extends AbstractMojo implements TranslatorLogger {

  private final MavenProject project;

  private final MavenSession session;

  private final ArtifactResolver artifactResolver;

  /**
   * The JAR file to translate. By default, it is the project output JAR in the build directory.
   */
  @Parameter(property = "j2z80.jarFile",
      defaultValue = "${project.build.directory}${file.separator}${project.build.finalName}.jar")
  private File jarFile;

  /**
   * Target output formats produced by the translation.
   */
  @Parameter(property = "j2z80.formats")
  private Set<Format> formats = Set.of(Format.A80);

  /**
   * Start address of the translated program in the target address space (0..65535).
   */
  @Parameter(property = "j2z80.startAddress", defaultValue = "28672")
  private int startAddress;

  /**
   * Initial stack pointer value for the generated program (0..65535).
   */
  @Parameter(property = "j2z80.stackTop", defaultValue = "65533")
  private int stackTop;

  /**
   * Whether to log the generated assembly text in the Maven output.
   */
  @Parameter(property = "j2z80.logAsmText", defaultValue = "false")
  private boolean logAsmText;

  /**
   * Resource patterns to exclude from translation.
   */
  @Parameter(property = "j2z80.excludeResources")
  private List<String> excludeResources = List.of();

  /**
   * Optimization level applied to the translated output.
   */
  @Parameter(property = "j2z80.optimization")
  private OptimizationLevel optimization = OptimizationLevel.NONE;

  @Inject
  public TranslatorMojo(
      final MavenProject project,
      final MavenSession session,
      final ArtifactResolver artifactResolver
  ) {
    this.session = session;
    this.project = project;
    this.artifactResolver = artifactResolver;
  }

  public File getJarFile() {
    return this.jarFile;
  }

  public void setJarFile(final File jarFile) {
    this.jarFile = jarFile;
  }

  public int getStartAddress() {
    return this.startAddress;
  }

  public void setStartAddress(final int startAddress) {
    this.startAddress = startAddress;
  }

  public int getStackTop() {
    return this.stackTop;
  }

  public void setStackTop(final int stackTop) {
    this.stackTop = stackTop;
  }

  public boolean isLogAsmText() {
    return this.logAsmText;
  }

  public void setLogAsmText(final boolean logAsmText) {
    this.logAsmText = logAsmText;
  }

  public List<String> getExcludeResources() {
    return this.excludeResources;
  }

  public void setExcludeResources(final List<String> excludeResources) {
    this.excludeResources = excludeResources;
  }

  public OptimizationLevel getOptimization() {
    return this.optimization;
  }

  public void setOptimization(final OptimizationLevel optimization) {
    this.optimization = optimization;
  }

  public Set<Format> getFormats() {
    return this.formats;
  }

  public void setFormats(final Set<Format> formats) {
    this.formats = formats;
  }

  @Override
  public void execute() throws MojoExecutionException {
    try {
      if (this.jarFile == null || !this.jarFile.isFile()) {
        throw new MojoExecutionException(
            "The JAR file to translate is missing or invalid: " + this.jarFile);
      }

      final List<Path> z80ClassPath =
          concat(this.getDependencyFilePaths().stream(), Stream.of(this.jarFile.toPath())).collect(
              Collectors.toList());
      final ClassLoader z80classLoader =
          JarClassLoaderFactory.create(z80ClassPath, this.getClass().getClassLoader());

      this.logInfo("Target formats: " + this.formats);
      this.logInfo("Target artifact name: " + this.project.getBuild().getFinalName());
      this.logInfo("Z80 classpath: " + z80ClassPath);

      final OptimizationLevel optimizationLevel =
          this.optimization == null ? OptimizationLevel.NONE : this.optimization;

      final TranslatorContext translator =
          new TranslatorImpl(this, optimizationLevel, z80ClassPath);
      final List<String> translatedAsmText =
          translator.translate(null, this.startAddress, this.stackTop, this.excludeResources,
              z80classLoader);

      if (this.logAsmText) {
        int lineIndex = 1;
        for (final String s : translatedAsmText) {
          this.logInfo("ASM: " + lineIndex + ": " + s);
          lineIndex++;
        }
      }

      if (this.formats.contains(Format.A80)) {
        final Path pathA80 = this.makeTargetFilePath("a80");
        this.logInfo("Writing A80 assembly file: " + pathA80);
        Files.write(pathA80, translatedAsmText, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE);
      }

      final Z80Asm targetA80 = new Z80Asm(translatedAsmText);
      final byte[] translatedBin = targetA80.process();

      if (this.formats.contains(Format.BIN)) {
        final Path pathBin = this.makeTargetFilePath("bin");
        this.getLog().info("Writing BIN output file: " + pathBin);
        Files.write(pathBin, translatedBin, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
      }

      if (this.formats.contains(Format.SNA)) {
        final Path pathSna = this.makeTargetFilePath("sna");
        this.getLog().info("Writing SNA48 output file: " + pathSna);
        final byte[] sna48 =
            new Sna48Writer(this.startAddress, this.stackTop, translatedBin).writeSna();
        Files.write(pathSna, sna48, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
      }
    } catch (final MojoExecutionException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new MojoExecutionException("Error during processing: " + ex.getMessage(), ex);
    }
  }

  private List<Path> getDependencyFilePaths() throws MojoExecutionException {
    final List<Path> foundFiles = new ArrayList<>();
    for (final Artifact artifact : this.project.getArtifacts()) {
      if ("z80".equalsIgnoreCase(artifact.getClassifier())) {
        try {
          final ArtifactResult art =
              this.artifactResolver.resolveArtifact(this.session.getProjectBuildingRequest(),
                  artifact);
          final File file = art.getArtifact().getFile();
          foundFiles.add(file.toPath());
        } catch (final ArtifactResolverException ex) {
          this.logError("Could not resolve Z80 dependency artifact: " + artifact);
          throw new MojoExecutionException(
              "Unable to resolve Z80 dependency artifact: " + artifact, ex);
        }
      }
    }

    return foundFiles;
  }

  private Path makeTargetFilePath(final String extension) {
    return Path.of(this.project.getBuild().getDirectory(),
        this.project.getBuild().getFinalName() + '.' + extension);
  }

  @Override
  public void logInfo(final String str) {
    this.getLog().info(str);
  }

  @Override
  public void logDebug(final String s) {
    this.getLog().debug(s);
  }

  @Override
  public void logWarning(final String str) {
    this.getLog().warn(str);
  }

  @Override
  public void logError(final String str) {
    this.getLog().error(str);
  }
}
