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
 * The interface describes a local label expectant, it works like an observer but only one time.
 *
 * @author Imgor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface LocalLabelExpectant {
  /**
   * This method will be called when the needed label is registered.
   *
   * @param asmTranslator the assembler translator, must not be null
   * @param labelName     the label name, must not be null
   * @param labelAddress  the label address
   */
  void onLabelIsAccessible(AsmTranslator asmTranslator, String labelName, long labelAddress);
}
