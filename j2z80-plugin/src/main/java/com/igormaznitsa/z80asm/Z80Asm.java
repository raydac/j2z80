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
package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.asmcommands.AbstractAsmCommand;
import com.igormaznitsa.z80asm.asmcommands.AsmCommandEND;
import com.igormaznitsa.z80asm.asmcommands.AsmCommandEQU;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import com.igormaznitsa.z80asm.exceptions.AsmTranslationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class implements a small Z80 assembler translator.
 * It allows only documented Z80 commands and supports only light expressions as command arguments.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class Z80Asm implements AsmTranslator {

  private final PositioningByteBuffer codeBuffer = new PositioningByteBuffer(0xFFFF);
  private final Set<String> nonAssignedLabels = new HashSet<>();
  private final LabelAddressContainer globalLabelMap = new LabelAddressContainer();
  private final LabelAddressContainer localLabelMap = new LabelAddressContainer(true);
  private final Map<String, List<LocalLabelExpectant>> localLabelExpectants = new HashMap<>();
  private final EquDirectiveContainer equContainer = new EquDirectiveContainer();
  private int programCounter;
  private int entryPoint;
  private boolean firstPassFlag;
  private String[] sources;

  public Z80Asm(final File file) throws IOException {
    this(Utils.readTextFileAsStringArray(file, "UTF-8"));
  }

  public Z80Asm(final File file, final String charSet) throws IOException {
    this(Utils.readTextFileAsStringArray(file, charSet));
  }

  public Z80Asm(final String[] sourceToBeCompiled) {
    Assert.assertNotNull("Source array must not be null", (Object) sourceToBeCompiled);
    final List<String> normalized = new ArrayList<>(sourceToBeCompiled.length);
    for (final String str : sourceToBeCompiled) {
      final String[] parsed = Utils.breakToLines(str);
      normalized.addAll(Arrays.asList(parsed));
    }
    sources = normalized.toArray(new String[0]);
  }

  private static boolean isLocalLabelName(final String labelName) {
    return labelName.charAt(0) == '@';
  }

  private void processEquCommands() {
    final boolean prevPassFlagState = firstPassFlag;
    firstPassFlag = false;

    globalLabelMap.setReplaceAllowed(true);
    final AbstractAsmCommand equCmnd = AbstractAsmCommand.findCommandForName("EQU");
    for (final EquDirectiveContainer.EquDirectiveRecord record : equContainer.getValuesAsList()) {
      setPC(record.getPC());
      record.getParsedAsmLine().setLabel(record.getAssociatedLabel());
      equCmnd.makeMachineCode(this, record.getParsedAsmLine());
    }
    equContainer.clear();
    globalLabelMap.setReplaceAllowed(false);
    firstPassFlag = prevPassFlagState;
  }

  public byte[] process() {
    firstPass();
    secondPass();
    return codeBuffer.toByteArray();
  }

  public AsmTranslator getContext() {
    return this;
  }

  private void resetInsideTables() {
    programCounter = 0;
    nonAssignedLabels.clear();
    entryPoint = 0;
    localLabelExpectants.clear();
    localLabelMap.clear();
  }

  private void throwExceptionForAsmErrorString(final String srcString, final int stringNumber, final Throwable cause) {
    throw new AsmTranslationException("Error ASM string detected", srcString, stringNumber, cause);
  }

  private void firstPass() {
    resetInsideTables();
    firstPassFlag = true;
    processSources();
    Assert.assertZero("After the first pass, the code buffer must be empty", codeBuffer.size());
    processEquCommands();
  }

  private void secondPass() {
    resetInsideTables();
    firstPassFlag = false;
    processSources();
  }

  private void processSources() {
    for (int strIndex = 0; strIndex < sources.length; strIndex++) {
      final String line = sources[strIndex];
      Assert.assertNotNull("Line at " + (strIndex + 1) + " is null", line);
      try {
        if (processOneLine(line, strIndex + 1)) {
          break;
        }
      } catch (Exception ex) {
        throwExceptionForAsmErrorString(line, strIndex + 1, ex);
      }
    }

    if (firstPassFlag) {
      for (final String label : nonAssignedLabels) {
        if (isLocalLabelName(label)) {
          registerLocalLabelAddress(label, getPC());
        } else {
          registerGlobalLabelAddress(label, getPC());
        }
      }
    }

    Assert.assertEmpty("Not-found some local labels " + Arrays.toString(localLabelExpectants.keySet().toArray()), localLabelExpectants);
  }

  // return true if need to interrupt processing, otherwise false
  private boolean processOneLine(final String asmString, final int stringIndex) {
    final ParsedAsmLine parsed = new ParsedAsmLine(asmString);

    boolean result = false;

    if (!parsed.isEmpty()) {
      if (parsed.hasOnlyLabel()) {
        nonAssignedLabels.add(parsed.getLabel());
      } else {
        final AbstractAsmCommand command = AbstractAsmCommand.findCommandForName(parsed.getCommand());

        Assert.assertNotNull("Unsupported command detected [" + parsed.getCommand() + ']', command);
        Assert.assertTrue("The command must be compatible in its argument number and their types [" + asmString + ']', command.getAllowedArgumentsNumber().check(parsed.getArgs()));

        final String currentLabel = parsed.getLabel();

        if (currentLabel != null) {
          nonAssignedLabels.add(currentLabel);
          parsed.setLabel(null);
        }

        if (command.isSpecialDirective()) {
          result = processSpecialDirective(command, asmString, stringIndex, parsed);
        } else {
          registerNonAssignedLabels();

          final int currentPC = getPC();
          final byte[] machineCode = command.makeMachineCode(this, parsed);

          writeCode(machineCode);
        }
      }
    }
    return result;
  }

  private String[] registerNonAssignedLabels() {
    List<String> result = null;
    final int address = getPC();
    if (firstPassFlag) {
      for (final String lbl : nonAssignedLabels) {
        if (isLocalLabelName(lbl)) {
          registerLocalLabelAddress(lbl, address);
        } else {
          registerGlobalLabelAddress(lbl, address);
        }
        if (result == null) {
          result = new ArrayList<>();
        }
        result.add(lbl);
      }
    } else {
      for (final String lbl : nonAssignedLabels) {
        if (isLocalLabelName(lbl)) {
          registerLocalLabelAddress(lbl, address);
        }
        if (result == null) {
          result = new ArrayList<>();
        }
        result.add(lbl);
      }
    }
    nonAssignedLabels.clear();

    return result == null ? new String[0] : result.toArray(new String[nonAssignedLabels.size()]);
  }

  private boolean processSpecialDirective(final AbstractAsmCommand asmCommand, final String rawString, final int strIndex, final ParsedAsmLine parsed) {
    if (asmCommand instanceof AsmCommandEQU) {
      if (firstPassFlag) {
        Assert.assertNotEmpty("Each EQU directive must be labeled [" + rawString + ']', nonAssignedLabels);
        final int address = getPC();
        for (final String lbl : registerNonAssignedLabels()) {
          equContainer.addRecord(lbl, parsed, address);
        }
      }
    } else {
      if (asmCommand instanceof AsmCommandEND) {
        return true;
      } else {
        registerNonAssignedLabels();
        final byte[] compiled = asmCommand.makeMachineCode(this, parsed);
        final int address = getPC();
        writeCode(compiled);
      }
    }
    return false;
  }

  @Override
  public Integer findLabelAddress(final String label) {
    if (firstPassFlag) {
      return getPC();
    } else {
      if (isLocalLabelName(label)) {
        return localLabelMap.hasLabel(label) ? localLabelMap.getLabelAddress(label) : null;
      } else {
        return globalLabelMap.hasLabel(label) ? globalLabelMap.getLabelAddress(label) : null;
      }
    }
  }

  @Override
  public void registerGlobalLabelAddress(final String label, final int address) {
    Assert.assertNotNull("Label must not be null", label);
    Assert.assertGlobalLabelName(label);

    Assert.assertAddress(address);

    globalLabelMap.registerLabel(label, address);
  }

  @Override
  public void registerLocalLabelAddress(final String label, final int address) {
    Assert.assertNotNull("Label name must not be null", label);
    Assert.assertLocalLabelName(label);
    Assert.assertAddress(address);

    localLabelMap.registerLabel(label, address);

    final List<LocalLabelExpectant> listeners = localLabelExpectants.get(label);
    if (listeners != null) {
      for (final LocalLabelExpectant listener : listeners) {
        listener.onLabelIsAccessible(this, label, address);
      }
      localLabelExpectants.remove(label);
    }
  }

  @Override
  public void registerLocalLabelExpectant(final String label, final LocalLabelExpectant expectant) {
    Assert.assertNotNull("Arguments must not contain null", label, expectant);
    Assert.assertLocalLabelName(label);

    List<LocalLabelExpectant> listeners = localLabelExpectants.computeIfAbsent(label, k -> new ArrayList<>());
    listeners.add(expectant);
  }

  @Override
  public int getPC() {
    return programCounter;
  }

  @Override
  public void setPC(final int newPCValue) {
    Assert.assertAddress(newPCValue);
    programCounter = newPCValue;
  }

  @Override
  public void writeCode(final byte[] code) {
    if (firstPassFlag) {
      programCounter += code.length;
    } else {
      codeBuffer.write(programCounter, code);
      programCounter += code.length;
      Assert.assertAddress(programCounter);
    }
  }

  @Override
  public int getCodeSize() {
    return codeBuffer.size();
  }

  @Override
  public int getEntryPoint() {
    return entryPoint;
  }

  @Override
  public void setEntryPoint(final int address) {
    Assert.assertAddress(address);
    entryPoint = address;
  }

  @Override
  public void printText(final String info) {
    if (System.out != null) {
      System.out.println(info);
    }
  }

  @Override
  public int getDataOffset() {
    return codeBuffer.getDataStartOffset();
  }

  @Override
  public void clearLocalLabels() {
    Assert.assertEmpty("There must not be any waiting expectant for local label " + Arrays.toString(localLabelExpectants.keySet().toArray()), localLabelExpectants);
    localLabelMap.clear();
  }
}
