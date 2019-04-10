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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The class allows to parse an assemler command line and split it to three parts: label, command and arguments.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@SuppressWarnings("serial")
public class ParsedAsmLine {

  private static final String[] SPEC_ARGS = new String[] {
      "A", "B", "C", "D", "E", "H", "L",
      "AF", "AF'", "BC", "DE", "HL",
      "NZ", "Z", "NC", "C", "PO", "PE", "M", "P",
      "SP", "(SP)", "IX", "IY", "(C)", "(HL)"
  };
  private static final Set<String> SPECIAL_ARG_SET = new HashSet<String>(Arrays.asList(SPEC_ARGS));
  private static final String[] EMPTY_ARRAY = new String[0];
  private final String command;
  private final String[] arguments;
  private final String signature;
  private final int hashCode;
  private String label;

  public ParsedAsmLine(final String label, final String command, final String... args) {
    this.label = label == null ? null : label.trim();
    if (command != null) {
      this.command = command.trim().toUpperCase(Locale.ENGLISH);
    } else {
      this.command = "";
    }

    if (args == null || args.length == 0) {
      this.arguments = EMPTY_ARRAY;
    } else {
      this.arguments = args.clone();
      for (int index = 0; index < this.arguments.length; index++) {
        this.arguments[index] = this.arguments[index].trim().toUpperCase(Locale.ENGLISH);
      }
    }
    this.signature = makeSignatureFromNormalizedArgs(this.arguments);
    this.hashCode = this.command.hashCode() * 31 + signature.hashCode();
  }

  public ParsedAsmLine(final String asmString) {
    final String trimmed = asmString.trim();
    if (trimmed.isEmpty() || trimmed.charAt(0) == ';') {
      hashCode = 0;
      command = "";
      label = null;
      arguments = EMPTY_ARRAY;
      signature = makeSignatureFromNormalizedArgs(arguments);
      return;
    }

    final String[] splitted = splitToParts(asmString);

    if (splitted[0] == null) {
      label = null;
    } else {
      label = checkLabel(splitted[0]);
    }

    command = splitted[1];

    if (splitted[2].isEmpty()) {
      arguments = EMPTY_ARRAY;
    } else {
      arguments = splitArguments(splitted[2]);
    }

    signature = makeSignatureFromNormalizedArgs(arguments);

    hashCode = command.hashCode() * 31 + signature.hashCode();
  }

  private static int findLabelPosition(final String line) {
    boolean atStr = false;
    int result = -1;
    int pos = 0;
    for (final char chr : line.toCharArray()) {
      if (atStr) {
        if (chr == '\"') {
          atStr = false;
        }
      } else {
        if (chr == '\"') {
          atStr = true;
        } else if (chr == ':') {
          result = pos;
          break;
        }
      }
      pos++;
    }
    return result;
  }

  private static int findFirstSpaceAfterCommand(final String line) {
    boolean foundNonSpace = false;
    boolean atStr = false;
    int result = -1;
    int pos = 0;
    for (final char chr : line.toCharArray()) {
      if (atStr) {
        if (chr == '\"') {
          atStr = false;
        }
      } else {
        if (chr == '\"') {
          atStr = true;
        } else if (Character.isWhitespace(chr)) {
          if (foundNonSpace) {
            result = pos;
            break;
          }
        } else {
          if (chr == ';') {
            break;
          }
          foundNonSpace = true;
        }
      }
      pos++;
    }
    return result;
  }

  private static String removeComment(final String line) {
    boolean atStr = false;
    final StringBuilder result = new StringBuilder();
    for (final char chr : line.toCharArray()) {
      if (atStr) {
        result.append(chr);
        if (chr == '\"') {
          atStr = false;
        }
      } else {
        if (chr == ';') {
          break;
        } else {
          result.append(chr);
          atStr = chr == '\"';
        }
      }
    }
    return result.toString();
  }

  private static String[] splitToParts(final String line) {
    final String[] result = new String[3];
    String str = line;
    final int labelPos = findLabelPosition(line);

    if (labelPos >= 0) {
      result[0] = normalizeString(str.substring(0, labelPos), false);
      str = str.substring(labelPos + 1);
    }

    final int commandSpacePos = findFirstSpaceAfterCommand(str);
    if (commandSpacePos < 0) {
      result[1] = normalizeString(removeComment(str), true);
      result[2] = "";
    } else {
      result[1] = normalizeString(str.substring(0, commandSpacePos), true);
      result[2] = normalizeString(removeComment(str.substring(commandSpacePos)), false);

    }

    return result;
  }

  private static String[] splitArguments(final String normalArguments) {
    final List<String> resultList = new ArrayList<String>();
    final StringBuilder buffer = new StringBuilder();

    boolean atString = false;
    boolean commentMet = false;
    for (final char chr : normalArguments.toCharArray()) {
      if (commentMet) {
        break;
      }
      if (Character.isSpaceChar(chr)) {
        if (buffer.length() == 0) {
          continue;
        } else if (!atString) {
          continue;
        }
      }
      switch (chr) {
        case '\"': {
          if (atString) {
            atString = false;
          } else {
            atString = true;
          }
          buffer.append("\"");
        }
        break;
        case ',': {
          if (atString) {
            buffer.append(',');
          } else {
            resultList.add(buffer.toString());
            buffer.setLength(0);
          }
        }
        break;
        case ';': {
          if (atString) {
            buffer.append(';');
          } else {
            resultList.add(buffer.toString());
            buffer.setLength(0);
            commentMet = true;
          }
        }
        break;
        default: {
          buffer.append(chr);
        }
        break;
      }
    }
    if (buffer.length() > 0) {
      resultList.add(buffer.toString());
    }

    // normalize arguments
    for (int i = 0; i < resultList.size(); i++) {
      final String original = resultList.get(i);
      final String upperCased = original.toUpperCase(Locale.ENGLISH);
      if (upperCased.startsWith("(IX") || upperCased.startsWith("(IY") && (upperCased.length() > 3 && (upperCased.charAt(3) == '-' || upperCased.charAt(3) == '+'))) {
        // replace the first part
        resultList.set(i, upperCased.substring(0, 3) + original.substring(3));
      } else if (SPECIAL_ARG_SET.contains(upperCased)) {
        resultList.set(i, upperCased);
      }
    }

    return resultList.toArray(new String[resultList.size()]);
  }

  private static String makeSignatureFromNormalizedArgs(final String[] arguments) {
    final StringBuilder buffer = new StringBuilder();
    for (final String arg : arguments) {
      if (buffer.length() > 0) {
        buffer.append(',');
      }
      buffer.append(arg);
    }
    return buffer.toString();
  }

  private static String normalizeString(final String value, final boolean makeUpperCased) {
    final StringBuilder result = new StringBuilder(value.length());
    boolean atStr = false;
    boolean interrupt = false;
    for (final char chr : value.toCharArray()) {
      if (interrupt) {
        break;
      }
      if (Character.isWhitespace(chr)) {
        if (atStr) {
          result.append(chr);
        } else {
          continue;
        }
      } else {
        switch (chr) {
          case '\"': {
            result.append(chr);
            atStr = atStr ? false : true;
          }
          break;
          case ';': {
            if (atStr) {
              result.append(chr);
            } else {
              interrupt = true;
            }
          }
          break;
          default: {
            if (atStr || !makeUpperCased) {
              result.append(chr);
            } else {
              result.append(Character.toUpperCase(chr));
            }
          }
        }
      }
    }
    return result.toString();
  }

  private static String checkLabel(final String label) {
    Assert.assertNotEmpty("Label must not be an empty string", label);

    final char firstChar = label.charAt(0);
    if (firstChar == '#' || firstChar == '%') {
      throw new IllegalArgumentException("The label start char can be wrong recognized [" + firstChar + ']');
    }

    try {
      Long.parseLong(label);
      throw new IllegalArgumentException("Label is like a number [" + label + ']');
    } catch (NumberFormatException ex) {
    }

    for (final char chr : label.toCharArray()) {
      switch (chr) {
        case ':':
        case ';':
        case '-':
        case '+':
          throw new IllegalArgumentException("Label contains a symbol which can be wrong recognized [" + chr + ']');
        default:
          Assert.assertFalse("Label must not contain a whitespace char", Character.isWhitespace(chr));
      }
    }

    return label;
  }

  private static boolean safeEquals(final Object obj1, final Object obj2) {
    if (obj1 == null) {
      return obj2 == null;
    } else if (obj2 == null) {
      return obj1 == null;
    } else {
      return obj1.equals(obj2);
    }
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getCommand() {
    return command;
  }

  public String[] getArgs() {
    return arguments;
  }

  public String getSignature() {
    return signature;
  }

  public boolean hasOnlyLabel() {
    return label != null && command.isEmpty() && arguments.length == 0;
  }

  public boolean isEmpty() {
    return label == null && command.isEmpty() && arguments.length == 0;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }

    if (obj.getClass() == ParsedAsmLine.class) {
      final ParsedAsmLine that = (ParsedAsmLine) obj;

      return safeEquals(this.label, that.label)
          && safeEquals(this.command, that.command)
          && Arrays.equals(this.arguments, that.arguments);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder(32);

    if (label != null) {
      result.append(label).append(": ");
    }

    if (!command.isEmpty()) {
      result.append(command).append(" ");
    }

    for (int index = 0; index < arguments.length; index++) {
      if (index > 0) {
        result.append(',');
      }
      result.append(arguments[index]);
    }

    return result.toString();
  }
}
