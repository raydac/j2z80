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

import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.GOTO_W;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFGE;
import org.apache.bcel.generic.IFGT;
import org.apache.bcel.generic.IFLE;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.bcel.generic.IF_ACMPNE;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.IF_ICMPGT;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IF_ICMPNE;
import org.junit.Test;

public class TestBranches extends AbstractJVMBranchTest {
  @Test(timeout = 3000L)
  public void testGOTO() throws Exception {
    assertTrueCondition(GOTO.class);
  }

  @Test(timeout = 3000L)
  public void testGOTOW() throws Exception {
    assertTrueCondition(GOTO_W.class);
  }

  @Test(timeout = 3000L)
  public void testIFNULL_true() throws Exception {
    push(0);
    assertTrueCondition(IFNULL.class);
  }

  @Test(timeout = 3000L)
  public void testIFNULL_false() throws Exception {
    push(0xFFFF);
    assertFalseCondition(IFNULL.class);
  }

  @Test(timeout = 3000L)
  public void testIFNONNULL_true() throws Exception {
    push(1);
    assertTrueCondition(IFNONNULL.class);
  }

  @Test(timeout = 3000L)
  public void testIFNONNULL_false() throws Exception {
    push(0);
    assertFalseCondition(IFNONNULL.class);
  }

  @Test(timeout = 3000L)
  public void testIFACMPEQ_true() throws Exception {
    push(0xA523);
    push(0xA523);
    assertTrueCondition(IF_ACMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFACMPEQ_false() throws Exception {
    push(23112);
    push(-23112);
    assertFalseCondition(IF_ACMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFACMPNE_true() throws Exception {
    push(0xA523);
    push(0xA522);
    assertTrueCondition(IF_ACMPNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFACMPNE_false() throws Exception {
    push(0xA522);
    push(0xA522);
    assertFalseCondition(IF_ACMPNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPEQ_ZERO() throws Exception {
    push(0);
    push(0);
    assertTrueCondition(IF_ICMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPEQ_NEGATIVE() throws Exception {
    push(-23234);
    push(-23234);
    assertTrueCondition(IF_ICMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPEQ_POSITIVE() throws Exception {
    push(23234);
    push(23234);
    assertTrueCondition(IF_ICMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPEQ_false() throws Exception {
    push(-23234);
    push(23234);
    assertFalseCondition(IF_ICMPEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPNE_trueTwoPositiveNumbers() throws Exception {
    push(1234);
    push(1235);
    assertTrueCondition(IF_ICMPNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPNE_trueForPositiveAndNegative() throws Exception {
    push(5623);
    push(-6123);
    assertTrueCondition(IF_ICMPNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPNE_false() throws Exception {
    push(0x7F12);
    push(0x7F12);
    assertFalseCondition(IF_ICMPNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_trueZeroLessThanPositive() throws Exception {
    push(0);
    push(2000);
    assertTrueCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_trueTwoPositives() throws Exception {
    push(1000);
    push(2000);
    assertTrueCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_trueTwoNegatives() throws Exception {
    push(-3000);
    push(-2000);
    assertTrueCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_trueNegativeAndPositive() throws Exception {
    push(-2344);
    push(2343);
    assertTrueCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_falseBecauseEqual() throws Exception {
    push(2343);
    push(2343);
    assertFalseCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_falsePositives() throws Exception {
    push(2345);
    push(2343);
    assertFalseCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_falseNegatives() throws Exception {
    push(-2343);
    push(-2345);
    assertFalseCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLT_falsePositiveNegative() throws Exception {
    push(2343);
    push(-2345);
    assertFalseCondition(IF_ICMPLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueFirstLessThanSecond() throws Exception {
    push(12212);
    push(12421);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueNegatives() throws Exception {
    push(-2344);
    push(-2343);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueNegativePositive() throws Exception {
    push(-2344);
    push(2343);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueForPositiveEqual() throws Exception {
    push(2344);
    push(2344);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueForNegativeEqual() throws Exception {
    push(-2344);
    push(-2344);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_trueForZero() throws Exception {
    push(0);
    push(0);
    assertTrueCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_falsePositives() throws Exception {
    push(2);
    push(1);
    assertFalseCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPLE_falseNegatives() throws Exception {
    push(-1);
    push(-2);
    assertFalseCondition(IF_ICMPLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGT_truePositives() throws Exception {
    push(18799);
    push(18788);
    assertTrueCondition(IF_ICMPGT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGT_trueNegatives() throws Exception {
    push(-18788);
    push(-18799);
    assertTrueCondition(IF_ICMPGT.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_trueForZero() throws Exception {
    push(0);
    push(0);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_truePositivesFirstGreat() throws Exception {
    push(19567);
    push(19566);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_truePositivesEqual() throws Exception {
    push(19566);
    push(19566);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_trueNegativesFirstGreat() throws Exception {
    push(-19566);
    push(-19567);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_trueNegativesEqual() throws Exception {
    push(-19566);
    push(-19566);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_truePositiveNegative() throws Exception {
    push(9566);
    push(-9566);
    assertTrueCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_falsePositives() throws Exception {
    push(100);
    push(200);
    assertFalseCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFICMPGE_falseNegatives() throws Exception {
    push(-200);
    push(-100);
    assertFalseCondition(IF_ICMPGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFEQ_true() throws Exception {
    push(0);
    assertTrueCondition(IFEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFEQ_false() throws Exception {
    push(12345);
    assertFalseCondition(IFEQ.class);
  }

  @Test(timeout = 3000L)
  public void testIFNE_true() throws Exception {
    push(0x1234);
    assertTrueCondition(IFNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFNE_false() throws Exception {
    push(0);
    assertFalseCondition(IFNE.class);
  }

  @Test(timeout = 3000L)
  public void testIFLT_true() throws Exception {
    push(-1234);
    assertTrueCondition(IFLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFLT_falseForZero() throws Exception {
    push(0);
    assertFalseCondition(IFLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFLT_falseForPositive() throws Exception {
    push(111);
    assertFalseCondition(IFLT.class);
  }

  @Test(timeout = 3000L)
  public void testIFLE_trueForNegative() throws Exception {
    push(-1234);
    assertTrueCondition(IFLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFLE_trueForZero() throws Exception {
    push(0);
    assertTrueCondition(IFLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFLE_falseForPositive() throws Exception {
    push(1112);
    assertFalseCondition(IFLE.class);
  }

  @Test(timeout = 3000L)
  public void testIFGT_true() throws Exception {
    push(1234);
    assertTrueCondition(IFGT.class);
  }

  @Test(timeout = 3000L)
  public void testIFGT_falseForZero() throws Exception {
    push(0);
    assertFalseCondition(IFGT.class);
  }

  @Test(timeout = 3000L)
  public void testIFGT_falseForNegative() throws Exception {
    push(-1234);
    assertFalseCondition(IFGT.class);
  }

  @Test(timeout = 3000L)
  public void testIFGE_trueForPositive() throws Exception {
    push(1234);
    assertTrueCondition(IFGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFGE_trueForZero() throws Exception {
    push(0);
    assertTrueCondition(IFGE.class);
  }

  @Test(timeout = 3000L)
  public void testIFGE_falseForNegative() throws Exception {
    push(-1234);
    assertFalseCondition(IFGE.class);
  }


}
