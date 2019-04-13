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

import org.apache.bcel.generic.LOOKUPSWITCH;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLOOKUPSWITCH extends AbstractJVMSelectTest {
  @Test(timeout = 3000L)
  public void testLookupSwitch_FirstCase() throws Exception {
    executeSelectInstruction(LOOKUPSWITCH.class, 1000, new int[] {1000, 0, 2000});
    assertEquals(1000, (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testLookupSwitch_LastCase() throws Exception {
    executeSelectInstruction(LOOKUPSWITCH.class, 2000, new int[] {1000, 0, 2000});
    assertEquals(2000, (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testLookupSwitch_MiddleCase() throws Exception {
    executeSelectInstruction(LOOKUPSWITCH.class, -100, new int[] {1000, -100, 2000});
    assertEquals(-100, (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testLookupSwitch_NonListedCase() throws Exception {
    executeSelectInstruction(LOOKUPSWITCH.class, -101, new int[] {1000, -100, 2000});
    assertEquals(0xFFFF, pop());
    assertStackEmpty();
  }

}
