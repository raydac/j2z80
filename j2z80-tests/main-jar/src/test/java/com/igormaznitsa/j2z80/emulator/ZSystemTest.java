/*
 * Copyright 2012-2026 Igor Maznitsa.
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

package com.igormaznitsa.j2z80.emulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import j2z80.ZSystem;
import org.apache.bcel.generic.Type;
import org.junit.Test;

public class ZSystemTest {

  @Test
  public void memoryAndIoMethodsUseUnsignedSixteenBitAddresses() {
    final JavaZ80Run run = JavaZ80Run.mainClass("demo.SystemOps")
        .withRuntimeClasspath()
        .file("demo/SystemOps.java", """
            package demo;
            
            import j2z80.ZSystem;
            
            public class SystemOps {
              public static int memoryValue;
              public static int portValue;
            
              public static void mainz() {
                ZSystem.poke(-1, (byte) 0x80);
                memoryValue = ZSystem.peek(-1);
                portValue = ZSystem.in(-292);
                ZSystem.out(-21555, (byte) 0xA5);
                ZSystem.im(2);
                ZSystem.im(7);
              }
            }
            """)
        .execute();

    assertEquals(-128, run.staticInt("demo.SystemOps", "memoryValue"));
    assertEquals(-1, run.staticInt("demo.SystemOps", "portValue"));
    assertEquals(0xFEDC, run.lastInputPort());
    assertEquals(0xABCD, run.lastOutputPort());
    assertEquals(0xA5, run.lastOutputValue());
  }

  @Test
  public void haltEmitsTheZ80HaltInstruction() {
    assertEquals(
        java.util.List.of("HALT"),
        new ZSystem().generateInvocation(null, "halt", new Type[0], Type.VOID)
    );
    assertEquals(
        java.util.List.of("EI"),
        new ZSystem().generateInvocation(null, "ei", new Type[0], Type.VOID)
    );
    assertEquals(
        java.util.List.of("DI"),
        new ZSystem().generateInvocation(null, "di", new Type[0], Type.VOID)
    );
  }

  @Test
  public void interruptModeDispatchUsesTheZ80ImmediateModeInstructions() {
    assertEquals(
        java.util.List.of("POP BC", "CALL ___ZSYSTEM_SET_INTERRUPT_MODE"),
        new ZSystem().generateInvocation(null, "im", new Type[] {Type.INT}, Type.VOID)
    );
    assertTrue(new ZSystem().getAdditionalText().containsAll(java.util.List.of(
        "IM 0", "IM 1", "IM 2", "___ZSYSTEM_SET_INTERRUPT_MODE_DONE:", "RET"
    )));
  }
}
