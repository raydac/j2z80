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
