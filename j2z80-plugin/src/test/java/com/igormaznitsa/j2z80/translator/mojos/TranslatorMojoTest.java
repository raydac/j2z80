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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.igormaznitsa.j2z80.translator.Format;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TranslatorMojoTest {

  @Rule
  public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void writesOnlyRequestedOutputFormats() throws Exception {
    final Path buildDirectory = this.temporaryFolder.getRoot().toPath();
    final Path inputJar = this.createInputJar(buildDirectory);
    final TranslatorMojo mojo = this.newMojo(buildDirectory, inputJar);
    mojo.setFormats(Set.of(Format.BIN));

    mojo.execute();

    assertTrue(Files.isRegularFile(buildDirectory.resolve("fixture.bin")));
    assertFalse(Files.exists(buildDirectory.resolve("fixture.a80")));
    assertFalse(Files.exists(buildDirectory.resolve("fixture.sna")));
  }

  @Test
  public void writesAssemblyBinaryAndSnaWhenAllFormatsAreRequested() throws Exception {
    final Path buildDirectory = this.temporaryFolder.getRoot().toPath();
    final Path inputJar = this.createInputJar(buildDirectory);
    final TranslatorMojo mojo = this.newMojo(buildDirectory, inputJar);
    mojo.setFormats(Set.of(
        Format.A80,
        Format.BIN,
        Format.SNA));

    mojo.execute();

    assertTrue(Files.isRegularFile(buildDirectory.resolve("fixture.a80")));
    assertTrue(Files.isRegularFile(buildDirectory.resolve("fixture.bin")));
    assertTrue(Files.isRegularFile(buildDirectory.resolve("fixture.sna")));
  }

  @Test
  public void reportsMissingInputJarAsMojoExecutionFailure() throws Exception {
    final Path buildDirectory = this.temporaryFolder.getRoot().toPath();
    final TranslatorMojo mojo =
        this.newMojo(buildDirectory, buildDirectory.resolve("missing.jar"));

    try {
      mojo.execute();
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("missing or invalid"));
      return;
    }
    throw new AssertionError("Expected MojoExecutionException for a missing input JAR");
  }

  private TranslatorMojo newMojo(final Path buildDirectory, final Path inputJar) {
    final MavenProject project = new MavenProject();
    final Build build = new Build();
    build.setDirectory(buildDirectory.toString());
    build.setFinalName("fixture");
    project.setBuild(build);

    final TranslatorMojo mojo =
        new TranslatorMojo(project, mock(MavenSession.class), mock(ArtifactResolver.class));
    mojo.setLog(new SystemStreamLog());
    mojo.setJarFile(inputJar.toFile());
    mojo.setStartAddress(0x7000);
    mojo.setStackTop(0xFFFD);
    return mojo;
  }

  private Path createInputJar(final Path buildDirectory) throws IOException {
    final Path inputJar = buildDirectory.resolve("input.jar");
    try (final JarOutputStream jarOutput = new JarOutputStream(Files.newOutputStream(inputJar))) {
      jarOutput.putNextEntry(new JarEntry(
          "com/igormaznitsa/j2z80/translator/mojos/MojoTranslationFixture.class"));
      try (final InputStream classFile = MojoTranslationFixture.class.getResourceAsStream(
          "MojoTranslationFixture.class")) {
        if (classFile == null) {
          throw new IOException("Could not load the Mojo translation fixture class");
        }
        classFile.transferTo(jarOutput);
      }
      jarOutput.closeEntry();
    }
    return inputJar;
  }
}
