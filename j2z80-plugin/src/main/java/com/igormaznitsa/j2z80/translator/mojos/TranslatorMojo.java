/*
 * Copyright 2012 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * This file is part of the JVM to Z80 translator project (hereinafter referred to as J2Z80).
 *
 * J2Z80 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * J2Z80 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2Z80.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.igormaznitsa.j2z80.translator.mojos;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maven mojo to translate a compiled Java classes from a Jar into Z80 binary
 * code through assembler stage.
 */
@Mojo(name = "translate",
    defaultPhase = LifecyclePhase.INSTALL,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class TranslatorMojo extends AbstractMojo implements TranslatorLogger {

  /**
   * Current maven project.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Component
  private ArtifactResolver artifactResolver;

  @Getter
  @Setter
  @Parameter(name = "jarFile", defaultValue = "${project.build.directory}${file.separator}${project.build.finalName}.jar")
  private File jarFile;

  @Getter
  @Setter
  @Parameter(name = "result", defaultValue = "${project.build.directory}${file.separator}${project.build.finalName}.bin")
  private File result;

  @Getter
  @Setter
  @Parameter(name = "format", defaultValue = "bin")
  private String format;

  @Getter
  @Setter
  @Parameter(name = "startAddress", defaultValue = "28672")
  private int startAddress;

  @Getter
  @Setter
  @Parameter(name = "stackTop", defaultValue = "65534")
  private int stackTop;

  @Getter
  @Setter
  @Parameter(name = "logAsmText", defaultValue = "false")
  private boolean logAsmText;

  @Getter
  @Setter
  @Parameter(name = "asmOutFile", defaultValue = "${project.build.directory}${file.separator}${project.build.finalName}.a80")
  private File asmOutFile;

  @Getter
  @Setter
  @Parameter(name = "excludeResources")
  private String[] excludeResources;

  @Getter
  @Setter
  @Parameter(name = "optimization", defaultValue = "none")
  private String optimization;

  @Parameter(name = "remoteRepositories", defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
  private List<ArtifactRepository> remoteRepositories;

  @Parameter(name = "session", defaultValue = "${session}", readonly = true, required = true)
  private MavenSession session;

  @Parameter(name = "execution", defaultValue = "${mojoExecution}", readonly = true, required = true)
  private MojoExecution execution;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      File[] classPath = getCompilationDependencies();

      classPath = Arrays.copyOf(classPath, classPath.length + 1);
      classPath[classPath.length - 1] = jarFile;

      logInfo("The result file format : " + format);
      logInfo("The result file name : " + result.getName());

      final OptimizationLevel optimizationLevel = OptimizationLevel.findForTextName(getOptimization());

      final TranslatorContext translator = new TranslatorImpl(this, optimizationLevel, classPath);
      final String[] translatedAsmText = translator.translate(null, startAddress, stackTop, getExcludeResources());

      if (logAsmText) {
        int lineIndex = 1;
        for (final String s : translatedAsmText) {
          logInfo("ASM: " + lineIndex + ": " + s);
          lineIndex++;
        }
      }

      if (asmOutFile != null) {
        logInfo("Save the result asm file as " + asmOutFile.getAbsolutePath());
        final BufferedWriter writer = new BufferedWriter(new FileWriter(asmOutFile, false));
        try {
          for (final String s : translatedAsmText) {
            writer.append(s);
            writer.newLine();
          }
          writer.flush();
        } finally {
          Utils.silentlyClose(writer);
        }
      }

      final Z80Asm asm = new Z80Asm(translatedAsmText);
      final byte[] generated = asm.process();

      if ("bin".equalsIgnoreCase(format)) {
        saveResultAsBin(generated);
      } else {
        throw new IllegalArgumentException("Usupported format [" + format + ']');
      }

      getLog().info("The result file has been saved as " + result.getAbsolutePath());
    } catch (Exception ex) {
      throw new MojoExecutionException("Exception during translation", ex);
    }
  }

  private File[] getCompilationDependencies() {
    final List<File> dependencyList = new ArrayList<>();
    for (final Artifact arty : this.project.getDependencyArtifacts()) {
      if ("z80".equalsIgnoreCase(arty.getClassifier())) {
        try {
          final ArtifactResult art = artifactResolver.resolveArtifact(project.getProjectBuildingRequest(), arty);
          final File file = art.getArtifact().getFile();
          logInfo("Detected Z80 dependency :" + file.getAbsolutePath());
          dependencyList.add(file);
        } catch (ArtifactResolverException ex) {
          logError("Can't resolve Z80 dependency artifact: " + arty);
          throw new RuntimeException(ex);
        }
      }
    }

    return dependencyList.toArray(new File[0]);
  }

  private void saveResultAsBin(final byte[] data) throws IOException {
    logInfo("Save the binary result as BIN file");
    final OutputStream out = new FileOutputStream(result);
    try {
      out.write(data);
      out.flush();
    } finally {
      Utils.silentlyClose(out);
    }
    logInfo("The saved file size is " + data.length + " bytes");
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
