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

import org.apache.bcel.generic.TABLESWITCH;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTABLESWITCH extends AbstractJVMSelectTest {

  @Test(timeout = 3000L)
  public void testTableSwitch_presentedFirstCase_sinceZero() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, 0, new int[] {0, 1, 2});
    assertEquals(0, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_presentedLastCase_sinceZero() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, 17, new int[] {-8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17});
    assertEquals(17, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_presentedSecondCase_sinceZero() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, 1, new int[] {0, 1, 2});
    assertEquals(1, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_nonPresentedCase_sinceZero() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, 3, new int[] {0, 1, 2});
    assertEquals(0xFFFF, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_nonPresentedNegativeCase_sinceZero() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, -1, new int[] {0, 1, 2});
    assertEquals(0xFFFF, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_presentedNegativeCase() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, -2, new int[] {-2, 0, 1, 2});
    assertEquals(-2, (short) pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTableSwitch_nonPresentedNegativeCase() throws Exception {
    executeSelectInstruction(TABLESWITCH.class, -3, new int[] {-4, -2, 0, 1, 2});
    assertEquals(-2, (short) pop());
    assertStackEmpty();
  }

}
