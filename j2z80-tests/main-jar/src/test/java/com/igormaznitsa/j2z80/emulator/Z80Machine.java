package com.igormaznitsa.j2z80.emulator;

import com.codingrodent.microprocessor.IBaseDevice;
import com.codingrodent.microprocessor.IMemory;
import com.codingrodent.microprocessor.Z80.CPUConstants.RegisterNames;
import com.codingrodent.microprocessor.Z80.Z80Core;

public final class Z80Machine implements IMemory, IBaseDevice {

  private static final int ADDRESS_SPACE = 0x10000;

  private final byte[] memory = new byte[ADDRESS_SPACE];
  private final Z80Core cpu = new Z80Core(this, this);

  public void load(final Program program) {
    final int origin = program.origin();
    final byte[] image = program.image();
    if (origin < 0 || image.length > ADDRESS_SPACE - origin) {
      throw new IllegalArgumentException("program does not fit at " + origin);
    }
    System.arraycopy(image, 0, this.memory, origin, image.length);
  }

  public void runUntilHalt(final int origin, final int instructionLimit) {
    this.cpu.reset();
    this.cpu.setProgramCounter(origin);
    int executed = 0;
    while (!this.cpu.getHalt()) {
      if (executed == instructionLimit) {
        throw new IllegalStateException(
            "halt was not reached after " + instructionLimit + " instructions");
      }
      this.cpu.executeOneInstruction();
      executed++;
    }
  }

  public void runUntil(final int origin, final int stopAddress, final int instructionLimit) {
    this.cpu.reset();
    this.cpu.setProgramCounter(origin);
    final int stop = stopAddress & 0xFFFF;
    int executed = 0;
    while (this.cpu.getProgramCounter() != stop) {
      if (executed == instructionLimit) {
        throw new IllegalStateException("stop address was not reached after " + instructionLimit
            + " instructions, pc=" + this.cpu.getProgramCounter());
      }
      this.cpu.executeOneInstruction();
      executed++;
    }
  }

  public int register(final RegisterNames name) {
    return this.cpu.getRegisterValue(name) & 0xFFFF;
  }

  public int wordAt(final int address) {
    return this.readWord(address);
  }

  @Override
  public int readByte(final int address) {
    return this.memory[address & 0xFFFF] & 0xFF;
  }

  @Override
  public void writeByte(final int address, final int value) {
    this.memory[address & 0xFFFF] = (byte) value;
  }

  @Override
  public int readWord(final int address) {
    final int location = address & 0xFFFF;
    return this.readByte(location) | (this.readByte(location + 1) << 8);
  }

  @Override
  public void writeWord(final int address, final int value) {
    final int location = address & 0xFFFF;
    this.writeByte(location, value);
    this.writeByte(location + 1, value >>> 8);
  }

  @Override
  public int IORead(final int port) {
    return 0xFF;
  }

  @Override
  public void IOWrite(final int port, final int value) {
  }
}
