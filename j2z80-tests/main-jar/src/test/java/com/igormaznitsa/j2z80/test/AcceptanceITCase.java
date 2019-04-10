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
package com.igormaznitsa.j2z80.test;

import com.igormaznitsa.j2z80test.Main;
import com.igormaznitsa.j2z80test.Main.AbstractTemplateGen;
import com.igormaznitsa.templategen.EvenPatternGenerator;
import com.igormaznitsa.templategen.OddPatternGenerator;
import j80.cpu.Z80;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static junit.framework.Assert.*;
import org.junit.Test;

public class AcceptanceITCase extends Z80 {

    private final byte[] memory = new byte[0x10000];
    private static final int START_ADDRESS = 29000;
    private int endAddress;

    protected int readLocalFrameVariable(final int index) {
        final int offset = index << 1;
        return (peekb(IX - offset + 1) << 8) | peekb(IX - offset);
    }

    protected void writeLocalFrameVariable(final int index, final int value) {
        final int offset = index << 1;
        pokeb(IX - offset + 1, value >>> 8);
        pokeb(IX - offset, value);
    }

    @Override
    public boolean step() {
        cycle = Integer.MAX_VALUE;
        if (PC == endAddress) {
            System.out.println("The end address has been met successfuly");
            return true;
        }
        return false;
    }

    @Test
    public void checkAsmLogFile() {
        final String filePath = System.getProperty("asmFile");
        assertNotNull("File must not be null", filePath);
        final File file = new File(filePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertTrue(file.length() > 0);
    }

    @Test
    public void ExecuteCompiledBlock() {
        try {
            final byte[] compiledBlock = loadCompiledBlock();
            System.out.println("Loaded compiled block " + compiledBlock.length + " bytes");
            int addr = START_ADDRESS;
            int len = compiledBlock.length;
            while (len > 0) {
                memory[addr] = compiledBlock[addr - START_ADDRESS];
                addr++;
                len--;
            }

            endAddress = (memory[START_ADDRESS + 9] & 0xFF) | ((memory[START_ADDRESS + 10] & 0xFF) << 8);

            PC = START_ADDRESS;

            System.out.println("Start execution from " + START_ADDRESS + " address, the end address is " + endAddress);
            exec(Integer.MAX_VALUE);

            // check the flag
            if ((short) peekw(Main.FLAG_ADDRESS) != Main.FLAG_DATA) {
                throw new IllegalArgumentException("Successful flag doesn't have needed value [#" + Integer.toHexString(peekw(Main.FLAG_ADDRESS)).toUpperCase() + ']');
            }



            assertOnlyIncludedTextResource("Check included text resource", "hello from resource", compiledBlock);
            assertPatternedArea("Must be ODD patterned", new OddPatternGenerator(), Main.START_ADDRESS_1 + 2, Main.BLOCK_LENGTH - 2);
            assertPatternedArea("Must be EVEN patterned", new EvenPatternGenerator(), Main.START_ADDRESS_2, Main.BLOCK_LENGTH);
            assertEmptyArea("Must be empty", Main.START_ADDRESS_3, Main.BLOCK_LENGTH);

            System.out.println("Execution completed");
        } catch (Exception ex) {
            throw new RuntimeException("Exception during processing", ex);
        }
    }

    private void assertOnlyIncludedTextResource(final String message, final String text, final byte[] block) {
        try {
            final String str = new String(block, "US-ASCII");
            int index = str.indexOf(text);
            if (index < 0) {
                fail("Resource string " + text + " must be presented");
            }
            
            index = str.indexOf(text, index+text.length());
            if (index>=0){
                fail("Resource string " + text + " has been met more than one time");
            }
            
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
        System.out.println(message + ".....OK");
    }

    private void assertEmptyArea(final String text, final int startAddress, final int length) {
        for (int addr = startAddress; addr < startAddress + length; addr += 2) {
            if (peekw(addr) != 0) {
                throw new Error(text + " at #" + Integer.toHexString(addr).toUpperCase() + " [" + peekw(addr) + " != 0]");
            }
        }
        System.out.println(text + ".....OK");
    }

    private void assertPatternedArea(final String text, final AbstractTemplateGen gen, final int startAddress, final int length) {
        for (int addr = startAddress; addr < startAddress + length; addr += 2) {
            final int etalon = gen.getValueForAddress(addr);
            if (((etalon ^ peekw(addr)) & 0xFFFF) != 0) {
                throw new Error(text);
            }
        }
        System.out.println(text + ".....OK");
    }

    private byte[] loadCompiledBlock() throws Exception {
        final String filePath = System.getProperty("translatedBinFile");
        final File file = new File(filePath);
        final DataInputStream inStream = new DataInputStream(new FileInputStream(file));
        try {
            final int len = (int) file.length();
            final byte[] result = new byte[len];
            inStream.readFully(result);
            return result;
        } finally {
            try {
                inStream.close();
            } catch (IOException ex) {
            }
        }

    }

    @Override
    public int getByteFromMemory(int address) {
        return memory[address] & 0xFF;
    }

    @Override
    public void setByteToMemory(int address, int value) {
        try {
            memory[address] = (byte) value;
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Current PC = #" + Integer.toHexString(PC).toUpperCase());
            System.out.println("Current IX = #" + Integer.toHexString(IX).toUpperCase());
            System.out.println("Current SP = #" + Integer.toHexString(SP).toUpperCase());
        }
    }
}
