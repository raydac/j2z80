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
package com.igormaznitsa.j2z80;

import com.igormaznitsa.j2z80.aux.Assert;
import com.igormaznitsa.j2z80.aux.Utils;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * It allow to make preprocessing of sources and text data in maven projects.
 *
 * @goal translate2z80 
 * @phase install 
 * @threadSafe 
 * @requiresProject
 * @requiresDependencyResolution compile
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class J2ZTranslator extends AbstractMojo implements TranslatorLogger {

    /**
     * The project.
     *
     * @parameter default-value="${project}" 
     * @required 
     * @readonly
     */
    private MavenProject project;

    /**
     * Local Maven repository where artifacts are cached during the build process.
     *
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * JAR File
     *
     * @parameter name="jar" default-value="${project.build.directory}/${project.build.finalName}.jar"
     * @readonly
     */
    private File jarFile;
    /**
     * Result file
     *
     * @parameter name="result" default-value="${project.build.directory}/${project.build.finalName}.bin"
     * @readonly
     */
    private File resultFile;
    /**
     * Output file format
     *
     * @parameter name="format" default-value="bin"
     * @readonly
     */
    private String format;
    /**
     * The start address of the generated code
     *
     * @parameter name="startAddress" default-value="28672" 
     * @readonly
     */
    private int startAddress;
    /**
     * The Stack top address
     *
     * @parameter name="stackTop" default-value="65534"
     * @readonly
     */
    private int stackTop;
    /**
     * Output assembler text into log
     *
     * @parameter name="logAsmText" default-value="false" 
     * @readonly
     */
    private boolean logAsmText;
    /**
     * The name of a file where the result asm text must be saved
     *
     * @parameter name="asmOutFile" default-value="${project.build.directory}/${project.build.finalName}.a80" 
     * @readonly
     */
    private File asmOutFile;

    /**
     * Patterns to be used to exclude binary resources from result compilation. Patterns use ANT style.
     *
     * @parameter name="excludeResources"
     */
    private String [] excludeResources;
    
    public void setExcludeResources(final String [] resources){
        this.excludeResources = resources;
    }
    
    public String [] getExcludeResources(){
        return this.excludeResources;
    }
    
    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(final ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public void setLogAsmText(final boolean flag){
        this.logAsmText = flag;
    }
    
    public void setAsmOutFile(final File file){
        this.asmOutFile = file;
    }
    
    public void setFormat(final String value) {
        this.format = value;
    }

    public void setJar(final File jar) {
        this.jarFile = jar;
    }

    public void setResult(final File result) {
        this.resultFile = result;
    }

    public void setStartAddress(final int address) {
        Assert.assertAddress(address);
        this.startAddress = address;
    }

    public void setStackTop(final int address) {
        Assert.assertAddress(address);
        this.stackTop = address;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File [] classPath = getCompilationDependencies();
            
            classPath = Arrays.copyOf(classPath, classPath.length+1);
            classPath[classPath.length-1] = jarFile;
            
            logInfo("The result file format : " + format);
            logInfo("The result file name : " + resultFile.getName());

            final TranslatorContext translator = new TranslatorImpl(this, classPath);
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

            getLog().info("The result file has been saved as " + resultFile.getAbsolutePath());
        } catch (Exception ex) {
            throw new MojoExecutionException("Exception during translation", ex);
        }
    }

    private File [] getCompilationDependencies(){
        final List<File> dependencyList = new ArrayList<File>();
        for(final Artifact arty : project.getDependencyArtifacts()){
                if ("z80".equalsIgnoreCase(arty.getClassifier())){
                    final Artifact art = localRepository.find(arty);
                    logInfo("Detected Z80 dependency :"+art.getFile().getAbsolutePath());
                    dependencyList.add(art.getFile());
                }
        }
        
        return dependencyList.toArray(new File[dependencyList.size()]);
    }
    
    private void saveResultAsBin(final byte[] data) throws IOException {
        logInfo("Save the binary result as BIN file");
        final OutputStream out = new FileOutputStream(resultFile);
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
