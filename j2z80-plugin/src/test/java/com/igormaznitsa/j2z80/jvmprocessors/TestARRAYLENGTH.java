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

package com.igormaznitsa.j2z80.jvmprocessors;

import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.Instruction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestARRAYLENGTH extends AbstractTestBasedOnMemoryManager {

  @Test(timeout = 3000L)
  public void testArrayLength() throws Exception {
    final int ARRAY_SIZE = 1234;
    push(ARRAY_SIZE);
    assertAllocateCommand(new Instruction[] {new ANEWARRAY(0), new ARRAYLENGTH()}, ARRAY_SIZE * 2 + 3);
    assertEquals(ARRAY_SIZE, pop());
    assertStackEmpty();
  }
}
