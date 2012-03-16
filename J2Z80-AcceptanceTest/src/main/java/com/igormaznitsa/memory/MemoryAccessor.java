package com.igormaznitsa.memory;

public class MemoryAccessor {
    public native static void writeWordToMemory(int address, int value);
    public native static int readWordFromMemory(int address);
}
