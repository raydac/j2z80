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
package com.igormaznitsa.z80asm;

/**
 * The interface describes an assembler translator.
 * 
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface AsmTranslator {

    /**
     * Find a label address for its name
     * @param labelName the label name, must not be null
     * @return the found address or null if the label is not defined
     */
    Integer findLabelAddress(String labelName);

    /**
     * Register a label address for a name in the global label table
     * @param labelName the label name, must not be null
     * @param address the label address
     */
    void registerGlobalLabelAddress(String labelName, int address);

    /**
     * Register a label address for a name in the local label table
     * @param labelName the label name, must not be null
     * @param address the label address
     */
    void registerLocalLabelAddress(String labelName, int address);

    /**
     * Register an object who needs to be informed only time when some label is registered
     * @param label the label name, must not be null
     * @param listener the listener to be informed about the label registration
     */
    void registerLocalLabelExpectant(String label, LocalLabelExpectant listener);

    /**
     * Get the current state of the program counter (PC)
     * @return the current state of the program counter
     */
    int getPC();

    /**
     * Set the state for the program counter (PC)
     * @param newPCValue the new value for the PC
     */
    void setPC(int newPCValue);

    /**
     * Write byte array into result data
     * @param code the array to be written, must not be null
     */
    void writeCode(byte[] code);

    /**
     * Get the current written code block size in bytes
     * @return the current code block size in bytes
     */
    int getCodeSize();

    /**
     * Get the offset in bytes to the first translated byte
     * @return the offset to the translated code block start
     */
    int getDataOffset();

    /**
     * Set the address to execute translated code
     * @param address the start address for the translated code
     */
    void setEntryPoint(int address);

    /**
     * Get the start address of the translated code
     * @return the start address
     */
    int getEntryPoint();

    /**
     * Print some text to an output device
     * @param text the text to be printed, must not be null
     */
    void printText(String text);

    /**
     * Remove all registered local variables from the inside table
     */
    void clearLocalLabels();
}
