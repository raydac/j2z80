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

import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.Instruction;
import org.junit.Test;

public class TestARRAYLENGTH extends AbstractTestBasedOnMemoryManager {

  @Test
  public void testArrayLength() throws Exception {
    final int ARRAY_SIZE = 1234;
    push(ARRAY_SIZE);
    assertAllocateCommand(new Instruction[] {new ANEWARRAY(0), new ARRAYLENGTH()}, ARRAY_SIZE * 2 + 3);
    assertEquals(ARRAY_SIZE, pop());
    assertStackEmpty();
  }
}
