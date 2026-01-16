/*
 * Copyright 2019 Igor Maznitsa.
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

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.translator.Format;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
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
import javax.inject.Inject;
import org.apache.maven.artifact.Artifact;
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
 * Maven mojo to translate a compiled Java classes from a Jar into Z80 binary
 * code through assembler stage.
 */
@Mojo(name = "translate",
    defaultPhase = LifecyclePhase.INSTALL,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class TranslatorMojo extends AbstractMojo implements TranslatorLogger {

  private final MavenProject project;

  private final ArtifactResolver artifactResolver;

  @Parameter(name = "jarFile", defaultValue = "${project.build.directory}${file.separator}${project.build.finalName}.jar")
  private File jarFile;

  @Parameter(name = "formats")
  private Set<Format> formats = Set.of(Format.A80);

  @Parameter(name = "startAddress", defaultValue = "28672")
  private int startAddress;

  @Parameter(name = "stackTop", defaultValue = "65533")
  private int stackTop;

  @Parameter(name = "logAsmText", defaultValue = "false")
  private boolean logAsmText;

  @Parameter(name = "excludeResources")
  private String[] excludeResources;

  @Parameter(name = "optimization", defaultValue = "none")
  private String optimization;

  @Inject
  public TranslatorMojo(
      final MavenProject project,
      final ArtifactResolver artifactResolver
  ) {
    this.project = project;
    this.artifactResolver = artifactResolver;
  }

  public File getJarFile() {
    return jarFile;
  }

  public void setJarFile(File jarFile) {
    this.jarFile = jarFile;
  }

  public int getStartAddress() {
    return startAddress;
  }

  public void setStartAddress(int startAddress) {
    this.startAddress = startAddress;
  }

  public int getStackTop() {
    return this.stackTop;
  }

  public void setStackTop(int stackTop) {
    this.stackTop = stackTop;
  }

  public boolean isLogAsmText() {
    return logAsmText;
  }

  public void setLogAsmText(boolean logAsmText) {
    this.logAsmText = logAsmText;
  }

  public String[] getExcludeResources() {
    return this.excludeResources;
  }

  public void setExcludeResources(String[] excludeResources) {
    this.excludeResources = excludeResources;
  }

  public String getOptimization() {
    return this.optimization;
  }

  public void setOptimization(String optimization) {
    this.optimization = optimization;
  }

  public Set<Format> getFormats() {
    return this.formats;
  }

  public void setFormats(Set<Format> formats) {
    this.formats = formats;
  }

  @Override
  public void execute() throws MojoExecutionException {
    try {
      List<File> classPath = new ArrayList<>(this.getCompilationDependencies());
      classPath.add(this.jarFile);

      logInfo("Target formats : " + this.formats);
      logInfo("Target final name : " + this.project.getBuild().getFinalName());

      final OptimizationLevel optimizationLevel =
          OptimizationLevel.findForTextName(this.optimization);

      final TranslatorContext translator = new TranslatorImpl(this, optimizationLevel, classPath);
      final List<String> translatedAsmText =
          translator.translate(null, startAddress, stackTop, this.excludeResources);

      if (this.logAsmText) {
        int lineIndex = 1;
        for (final String s : translatedAsmText) {
          logInfo("ASM: " + lineIndex + ": " + s);
          lineIndex++;
        }
      }

      if (this.formats.contains(Format.A80)) {
        final Path pathA80 = this.makeTargetFilePath("a80");
        this.logInfo("Writing A80 assembler file: " + pathA80);
        Files.write(pathA80, translatedAsmText, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
      }

      final Z80Asm targetA80 = new Z80Asm(translatedAsmText);
      final byte[] translatedBin = targetA80.process();

      if (this.formats.contains(Format.BIN)) {
        final Path pathBin = this.makeTargetFilePath("bin");
        this.getLog().info("Writing BIN file: " + pathBin);
        Files.write(pathBin, translatedBin);
      }

      if (this.formats.contains(Format.SNA)) {
        final Path pathSna = this.makeTargetFilePath("sna");
        this.getLog().info("Writing SNA48 file: " + pathSna);
        final byte[] sna48 =
            new Sna48Writer(this.startAddress, this.stackTop, translatedBin).writeSna();
        Files.write(pathSna, sna48);
      }
    } catch (Exception ex) {
      throw new MojoExecutionException("Error during processing: " + ex.getMessage(), ex);
    }
  }

  private List<File> getCompilationDependencies() {
    final List<File> dependencyList = new ArrayList<>();
    for (final Artifact arty : this.project.getDependencyArtifacts()) {
      if ("z80".equalsIgnoreCase(arty.getClassifier())) {
        try {
          final ArtifactResult art =
              artifactResolver.resolveArtifact(project.getProjectBuildingRequest(), arty);
          final File file = art.getArtifact().getFile();
          logInfo("Detected Z80 dependency :" + file.getAbsolutePath());
          dependencyList.add(file);
        } catch (ArtifactResolverException ex) {
          logError("Can't resolve Z80 dependency artifact: " + arty);
          throw new RuntimeException(ex);
        }
      }
    }

    return dependencyList;
  }

  private Path makeTargetFilePath(final String extension) {
    return Path.of(this.project.getBuild().getDirectory() + File.separator +
        this.project.getBuild().getFinalName() + '.' + extension);
  }

  @Override
  public void logInfo(final String str) {
    getLog().info(str);
  }

  @Override
  public void logWarning(final String str) {
    getLog().warn(str);
  }

  @Override
  public void logError(final String str) {
    getLog().error(str);
  }
}
