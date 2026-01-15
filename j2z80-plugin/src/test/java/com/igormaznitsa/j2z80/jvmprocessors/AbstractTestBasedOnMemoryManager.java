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
package com.igormaznitsa.j2z80.jvmprocessors;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.Z80Asm;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.junit.Before;
import org.powermock.reflect.Whitebox;

public abstract class AbstractTestBasedOnMemoryManager extends AbstractJvmCommandProcessorTest implements NeedsMemoryManager {

  protected final static String INSTANCEOFMANAGER_TEXT;
  protected final static String MEMORYMANAGER_TEXT;

  static {
    try {
      MEMORYMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "MEMORY_MANAGER.a80");
    } catch (IOException ex) {
      throw new Error("Can't read memory manager assembler text");
    }
    try {
      INSTANCEOFMANAGER_TEXT = Utils.readTextResource(AbstractJvmCommandProcessor.class, "INSTANCEOF_MANAGER.a80");
    } catch (IOException ex) {
      throw new Error("Can't read instanceof manager assembler text");
    }
  }

  private int memoryTopAddress;
  private int memoryPointerAddress;

  @Before
  @Override
  public void beforeTest() {
    SP = INIT_SP;
    PC = START_ADDRESS;
  }

  public String prepareMemoryManagerText() {
    return MEMORYMANAGER_TEXT;
  }

  private String generateAsmForAllocateCommand(final Instruction[] instructions) throws Exception {
    final StringWriter out = new StringWriter();

    for (final Instruction instruction : instructions) {
      final AbstractJvmCommandProcessor processor = AbstractJvmCommandProcessor.findProcessor(instruction.getClass());
      final InstructionHandle mockInstructionHandle = Whitebox.invokeMethod(InstructionHandle.class, "getInstructionHandle", instruction);
      processor.process(CLASS_PROCESSOR_MOCK, instruction, mockInstructionHandle, out);
    }
    return out.toString();
  }

  public int getInitialMemoryAddress() {
    return memoryTopAddress;
  }

  public int getAllocatedMemorySize() {
    return peekw(memoryPointerAddress) - memoryTopAddress;
  }

  public Z80Asm assertLinearExecutionToEnd(final String text, final int assertedMemory) {
    final String[] asmText = new String[] {
        getAsmBeforeAll(),
        "   org " + START_ADDRESS,
        getAsmPrefix(),
        text,
        END_LABEL + ": NOP",
        getAsmPostfix(),
        prepareMemoryManagerText()
    };

    for (final String str : asmText) {
      System.out.println(str);
    }

    final Z80Asm asm = new Z80Asm(Arrays.asList(asmText));

    final byte[] compiled = asm.process();

    System.arraycopy(compiled, 0, memory, asm.getDataOffset(), compiled.length);

    memoryTopAddress = asm.findLabelAddress(MEMORY_HEAP_START_AREA_LABEL).intValue();
    memoryPointerAddress = asm.findLabelAddress(VAR_MANAGER_TOP_POINTER).intValue();

    endAddress = asm.findLabelAddress(END_LABEL).intValue();
    exec(Integer.MAX_VALUE);
    assertEquals("Must allocate block size", assertedMemory, getAllocatedMemorySize());
    return asm;
  }


  public void assertAllocateCommand(final Instruction[] allocateInstruction, int assertedMemory) throws Exception {
    final String textForCommand = generateAsmForAllocateCommand(allocateInstruction);

    final String[] asmText = new String[] {
        getAsmBeforeAll(),
        "   org " + START_ADDRESS,
        getAsmPrefix(),
        textForCommand,
        END_LABEL + ": NOP",
        getAsmPostfix(),
        prepareMemoryManagerText()
    };

    for (final String str : asmText) {
      System.out.println(str);
    }

    final Z80Asm asm = new Z80Asm(Arrays.asList(asmText));

    final byte[] compiled = asm.process();

    System.arraycopy(compiled, 0, memory, asm.getDataOffset(), compiled.length);

    memoryTopAddress = asm.findLabelAddress(MEMORY_HEAP_START_AREA_LABEL).intValue();
    memoryPointerAddress = asm.findLabelAddress(VAR_MANAGER_TOP_POINTER).intValue();

    endAddress = asm.findLabelAddress(END_LABEL).intValue();
    exec(Integer.MAX_VALUE);
    assertEquals("Must allocate block size", assertedMemory, getAllocatedMemorySize());
  }
}
