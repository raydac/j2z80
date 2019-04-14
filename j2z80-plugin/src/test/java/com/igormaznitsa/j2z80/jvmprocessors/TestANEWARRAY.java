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

import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.SIPUSH;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestANEWARRAY extends AbstractTestBasedOnMemoryManager {

  @Test(timeout = 3000L)
  public void testArrayCreation() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new ANEWARRAY(0), new ARRAYLENGTH()}, 2003);
    assertEquals(1000, pop());
    assertStackEmpty();
  }

  @Test(timeout = 3000L)
  public void testTwoArrayCreation() throws Exception {
    push(1000);
    assertAllocateCommand(new Instruction[] {new ANEWARRAY(0), new POP(), new SIPUSH((short) 1000), new ANEWARRAY(0)}, 4006);
    pop();
    assertStackEmpty();
  }
}
