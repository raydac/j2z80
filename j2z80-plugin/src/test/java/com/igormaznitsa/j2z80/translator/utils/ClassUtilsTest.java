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
package com.igormaznitsa.j2z80.translator.utils;

import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.api.additional.NeedsATHROWManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEINTERFACEManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_INVOKEINTERFACE;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassUtilsTest {

  @Test
  public void testfindAllAdditionalBlocksInClass() {
    final Set<Class<? extends J2ZAdditionalBlock>> foundAdditions = ClassUtils.findAllAdditionalBlocksInClass(Processor_INVOKEINTERFACE.class);
    assertEquals("Must have 3 additional blocks", 3, foundAdditions.size());
    assertTrue("Must have " + NeedsATHROWManager.class.getCanonicalName(), foundAdditions.contains(NeedsATHROWManager.class));
    assertTrue("Must have " + NeedsINVOKEINTERFACEManager.class.getCanonicalName(), foundAdditions.contains(NeedsINVOKEINTERFACEManager.class));
    assertTrue("Must have " + NeedsMemoryManager.class.getCanonicalName(), foundAdditions.contains(NeedsMemoryManager.class));
  }

}
