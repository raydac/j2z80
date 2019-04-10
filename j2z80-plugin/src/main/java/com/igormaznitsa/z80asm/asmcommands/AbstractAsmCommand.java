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

package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The class describes an abstract Z80 assembler command.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@SuppressWarnings("serial")
public abstract class AbstractAsmCommand {

  protected static final byte[] EMPTY_ARRAY = new byte[0];
  private static final Map<String, Integer> REGISTER_ORDER = new HashMap<>();
  private static final Map<String, AbstractAsmCommand> COMMAND_MAP = new HashMap<>();
  private static final Set<String> REGISTER_NAME = new HashSet<>();
  private static final Set<String> REGISTER_NAME_16 = new HashSet<>();

  static {
    // init inside mapping and command list
    REGISTER_ORDER.put("A", Integer.valueOf(7));
    REGISTER_ORDER.put("B", Integer.valueOf(0));
    REGISTER_ORDER.put("C", Integer.valueOf(1));
    REGISTER_ORDER.put("D", Integer.valueOf(2));
    REGISTER_ORDER.put("E", Integer.valueOf(3));
    REGISTER_ORDER.put("H", Integer.valueOf(4));
    REGISTER_ORDER.put("L", Integer.valueOf(5));
    REGISTER_ORDER.put("(HL)", Integer.valueOf(6));

    REGISTER_NAME.addAll(Arrays.asList("A", "B", "C", "D", "E", "H", "L", "BC", "DE", "HL", "IX", "IY", "AF", "AF'", "R", "I", "(BC)", "(DE)", "(HL)", "SP", "(SP)"));
    REGISTER_NAME_16.addAll(Arrays.asList("BC", "DE", "HL", "SP", "IX", "IY"));
    addCommand("NOP");
    addCommand("ADC");
    addCommand("ADD");
    addCommand("SBC");
    addCommand("SUB");
    addCommand("AND");
    addCommand("XOR");
    addCommand("OR");
    addCommand("CP");
    addCommand("DAA");
    addCommand("CPL");
    addCommand("NEG");
    addCommand("CCF");
    addCommand("SCF");
    addCommand("INC");
    addCommand("DEC");
    addCommand("RLCA");
    addCommand("RLA");
    addCommand("RRCA");
    addCommand("RRA");
    addCommand("RLC");
    addCommand("RL");
    addCommand("RRC");
    addCommand("RR");
    addCommand("SLA");
    addCommand("SRA");
    addCommand("SRL");
    addCommand("RLD");
    addCommand("RRD");
    addCommand("BIT");
    addCommand("SET");
    addCommand("RES");
    addCommand("HALT");
    addCommand("DI");
    addCommand("EI");
    addCommand("IM");
    addCommand("CALL");
    addCommand("RET");
    addCommand("RETI");
    addCommand("RETN");
    addCommand("RST");
    addCommand("JP");
    addCommand("JR");
    addCommand("DJNZ");
    addCommand("IN");
    addCommand("INI");
    addCommand("INIR");
    addCommand("IND");
    addCommand("INDR");
    addCommand("OUT");
    addCommand("OUTI");
    addCommand("OUTD");
    addCommand("LD");
    addCommand("EX");
    addCommand("EXX");
    addCommand("POP");
    addCommand("PUSH");
    addCommand("LDI");
    addCommand("LDIR");
    addCommand("CPI");
    addCommand("LDD");
    addCommand("CPD");
    addCommand("CPIR");
    addCommand("OTIR");
    addCommand("LDDR");
    addCommand("CPDR");
    addCommand("OTDR");
    addCommand("EQU");
    addCommand("ENT");
    addCommand("DEFS");
    addCommand("DEFW");
    addCommand("DEFM");
    addCommand("DEFB");
    addCommand("ORG");
    addCommand("END");
    addCommand("ASSERT");
    addCommand("CLRLOC");
  }

  protected final Map<String, byte[]> PATTERN_CASES = new HashMap<>();

  protected static boolean doesNeedCalculation(final String arg) {
    return !isRegisterName(arg);
  }

  protected static String extractCalculatedPart(final String arg) {
    Assert.assertNotEmpty("Argument must not be empty", arg);

    String result;

    if (isIndexRegisterReference(arg) && arg.endsWith(")")) {
      result = arg.substring(3, arg.length() - 1);
    } else {
      if (arg.charAt(0) == '(') {
        Assert.assertTrue("Each bracket must be closed [" + arg + ']', arg.endsWith(")"));
        result = arg.substring(1, arg.length() - 1);
      } else {
        Assert.assertFalse("Each bracket must be opened [" + arg + ']', arg.endsWith(")"));
        result = arg;
      }
    }
    return result;
  }

  protected static boolean isRegisterName(final String normalizedName) {
    return REGISTER_NAME.contains(normalizedName);
  }

  protected static boolean isRegister16Name(final String normalizedName) {
    return REGISTER_NAME_16.contains(normalizedName);
  }

  protected static boolean isInBrakes(final String value) {
    return value.length() >= 2 && value.charAt(0) == '(' && value.endsWith(")");
  }

  protected static boolean isIndexRegisterReference(final String arg) {
    return arg.startsWith("(IX+")
        || arg.startsWith("(IX-")
        || arg.startsWith("(IY+")
        || arg.startsWith("(IY-");
  }

  protected static boolean isString(final String arg) {
    return arg.startsWith("\"") && arg.endsWith("\"");
  }

  protected static int calculateAddressOffset(final int jumpAddress, final int baseAddress) {
    final int result = jumpAddress - (baseAddress + 2);
    Assert.assertSignedByte(result);
    return result;
  }

  private static void addCommand(final String name) {
    final String className = AbstractAsmCommand.class.getPackage().getName() + ".AsmCommand" + name;
    try {
      final Class<? extends AbstractAsmCommand> commandClass = Class.forName(className).asSubclass(AbstractAsmCommand.class);
      final AbstractAsmCommand command = (AbstractAsmCommand) commandClass.getDeclaredConstructor().newInstance();
      Assert.assertTrue("A Command must have the same name as its class name [" + name + ']', command.getName().equals(name));
      COMMAND_MAP.put(command.getName(), command);
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("Can't find any class for the \'" + name + "\' command", ex);
    } catch (Exception ex) {
      throw new RuntimeException("Can't instantiate class for the \'" + name + "\' command", ex);
    }
  }

  public static AbstractAsmCommand findCommandForName(final String name) {
    return COMMAND_MAP.get(name);
  }

  protected void addCase(final String signature, final byte... codes) {
    Assert.assertNotNull("Signature must not be null", signature);
    Assert.assertNotNull("Code block must not be null", codes);
    Assert.assertNotEmpty("Code block must not be empty", codes);


    Assert.assertFalse("Signature must be unique", PATTERN_CASES.containsKey(signature));

    PATTERN_CASES.put(signature, codes);
  }

  protected byte[] getPatternCase(final String pattern) {
    final byte[] result = PATTERN_CASES.get(pattern);
    Assert.assertNotNull("A Case must be declared [" + pattern + ']', result);
    return result;
  }

  protected int getRegisterOrder(final String register) {
    int result = 6;
    if (!isIndexRegisterReference(register)) {
      final Integer order = REGISTER_ORDER.get(register);
      Assert.assertNotNull("Register name must be known [" + register + ']', order);
      result = order.intValue();
    }
    return result;
  }

  public abstract byte[] makeMachineCode(AsmTranslator context, ParsedAsmLine asm);

  public abstract String getName();

  public Arguments getAllowedArgumentsNumber() {
    return Arguments.NONE;
  }

  public boolean isSpecialDirective() {
    return false;
  }

  public enum Arguments {

    ZERO_ONE_OR_TWO,
    ANY,
    ONE_OR_TWO,
    NONE,
    NONE_OR_ONE,
    ONE,
    TWO,
    ONE_OR_MORE;

    public boolean check(final String[] args) {
      switch (this) {
        case NONE:
          return args.length == 0;
        case NONE_OR_ONE:
          return args.length == 0 || args.length == 1;
        case ZERO_ONE_OR_TWO:
          return args.length == 0 || args.length == 1 || args.length == 2;
        case ANY:
          return args != null;
        case ONE:
          return args.length == 1;
        case ONE_OR_MORE:
          return args.length >= 1;
        case ONE_OR_TWO:
          return args.length == 1 || args.length == 2;
        case TWO:
          return args.length == 2;
        default:
          throw new IllegalArgumentException("Unsupported argument value");
      }
    }
  }
}
