package com.igormaznitsa.j2z80;

import com.igormaznitsa.j2z80.aux.*;
import com.igormaznitsa.j2z80.translator.TranslatorImpl;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.*;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 * It allow to make preprocessing of sources and text data in maven projects.
 *
 * @goal translate2z80 
 * @phase install 
 * @threadSafe 
 * @requiresProject
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
            logInfo("The result file format : " + format);
            logInfo("The result file name : " + resultFile.getName());

            final TranslatorContext translator = new TranslatorImpl(jarFile, this);
            final String[] translatedAsmText = translator.translate(null, startAddress, stackTop);

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
