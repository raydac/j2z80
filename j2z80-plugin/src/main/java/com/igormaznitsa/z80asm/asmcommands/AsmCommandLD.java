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
package com.igormaznitsa.z80asm.asmcommands;

import com.igormaznitsa.j2z80.utils.Assert;
import com.igormaznitsa.z80asm.AsmTranslator;
import com.igormaznitsa.z80asm.expression.LightExpression;

public class AsmCommandLD extends AbstractAsmCommand {

  public AsmCommandLD() {
    super();
    addCase("A,A", (byte) 0x7F);
    addCase("A,B", (byte) 0x78);
    addCase("A,C", (byte) 0x79);
    addCase("A,D", (byte) 0x7A);
    addCase("A,E", (byte) 0x7B);
    addCase("A,H", (byte) 0x7C);
    addCase("A,L", (byte) 0x7D);
    addCase("A,(HL)", (byte) 0x7E);

    addCase("B,A", (byte) 0x47);
    addCase("B,B", (byte) 0x40);
    addCase("B,C", (byte) 0x41);
    addCase("B,D", (byte) 0x42);
    addCase("B,E", (byte) 0x43);
    addCase("B,H", (byte) 0x44);
    addCase("B,L", (byte) 0x45);
    addCase("B,(HL)", (byte) 0x46);

    addCase("C,A", (byte) 0x4F);
    addCase("C,B", (byte) 0x48);
    addCase("C,C", (byte) 0x49);
    addCase("C,D", (byte) 0x4A);
    addCase("C,E", (byte) 0x4B);
    addCase("C,H", (byte) 0x4C);
    addCase("C,L", (byte) 0x4D);
    addCase("C,(HL)", (byte) 0x4E);

    addCase("D,A", (byte) 0x57);
    addCase("D,B", (byte) 0x50);
    addCase("D,C", (byte) 0x51);
    addCase("D,D", (byte) 0x52);
    addCase("D,E", (byte) 0x53);
    addCase("D,H", (byte) 0x54);
    addCase("D,L", (byte) 0x55);
    addCase("D,(HL)", (byte) 0x56);

    addCase("E,A", (byte) 0x5F);
    addCase("E,B", (byte) 0x58);
    addCase("E,C", (byte) 0x59);
    addCase("E,D", (byte) 0x5A);
    addCase("E,E", (byte) 0x5B);
    addCase("E,H", (byte) 0x5C);
    addCase("E,L", (byte) 0x5D);
    addCase("E,(HL)", (byte) 0x5E);

    addCase("H,A", (byte) 0x67);
    addCase("H,B", (byte) 0x60);
    addCase("H,C", (byte) 0x61);
    addCase("H,D", (byte) 0x62);
    addCase("H,E", (byte) 0x63);
    addCase("H,H", (byte) 0x64);
    addCase("H,L", (byte) 0x65);
    addCase("H,(HL)", (byte) 0x66);

    addCase("L,A", (byte) 0x6F);
    addCase("L,B", (byte) 0x68);
    addCase("L,C", (byte) 0x69);
    addCase("L,D", (byte) 0x6A);
    addCase("L,E", (byte) 0x6B);
    addCase("L,H", (byte) 0x6C);
    addCase("L,L", (byte) 0x6D);
    addCase("L,(HL)", (byte) 0x6E);

    addCase("(HL),A", (byte) 0x77);
    addCase("(HL),B", (byte) 0x70);
    addCase("(HL),C", (byte) 0x71);
    addCase("(HL),D", (byte) 0x72);
    addCase("(HL),E", (byte) 0x73);
    addCase("(HL),H", (byte) 0x74);
    addCase("(HL),L", (byte) 0x75);

    addCase("A,(BC)", (byte) 0x0A);
    addCase("A,(DE)", (byte) 0x1A);

    addCase("(BC),A", (byte) 0x02);
    addCase("(DE),A", (byte) 0x12);

    addCase("I,A", (byte) 0xED, (byte) 0x47);
    addCase("A,I", (byte) 0xED, (byte) 0x57);

    addCase("R,A", (byte) 0xED, (byte) 0x4F);
    addCase("A,R", (byte) 0xED, (byte) 0x5F);

    addCase("SP,HL", (byte) 0xF9);
    addCase("SP,IX", (byte) 0xDD, (byte) 0xF9);
    addCase("SP,IY", (byte) 0xFD, (byte) 0xF9);
  }

  @Override
  public byte[] makeMachineCode(final AsmTranslator context, final ParsedAsmLine asm) {
    final String leftPart = asm.getArgs()[0];
    final String rightPart = asm.getArgs()[1];

    final boolean leftPartIsRegister = isRegisterName(leftPart) || "(HL)".equals(leftPart);
    final boolean rightPartIsRegister = isRegisterName(rightPart);

    if (leftPartIsRegister && rightPartIsRegister) {
      return getPatternCase(asm.getSignature());
    } else if (leftPartIsRegister && !rightPartIsRegister) {
      return getMachineCodeWhenLeftRegister(context, asm, leftPart, rightPart);
    } else if (!leftPartIsRegister && rightPartIsRegister) {
      return getMachineCodeWhenRightRegister(context, asm, leftPart, rightPart);
    } else {
      return nonregisterAtBothPart(context, asm, leftPart, rightPart);
    }
  }

  @Override
  public String getName() {
    return "LD";
  }

  private byte[] nonregisterAtBothPart(final AsmTranslator context, final ParsedAsmLine asm, final String leftPart, final String rightPart) {
    Assert.assertTrue("Unsupported LD arguments", isIndexRegisterReference(leftPart));
    final int offset = new LightExpression(context, this, asm, extractCalculatedPart(leftPart)).calculate();
    Assert.assertSignedByte(offset);

    Assert.assertFalse("Wrong pointer usage in the right part", isInBrakes(rightPart));

    final int data = new LightExpression(context, this, asm, extractCalculatedPart(rightPart)).calculate();
    Assert.assertUnsignedByte(data);

    final byte prefix = leftPart.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;
    return new byte[] {prefix, (byte) 0x36, (byte) offset, (byte) data};
  }

  private byte[] getMachineCodeWhenLeftRegister(final AsmTranslator context, final ParsedAsmLine asm, final String leftPart, final String rightPart) {

    byte[] result = null;

    if (isIndexRegisterReference(rightPart)) {
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(rightPart)).calculate();
      Assert.assertSignedByte(offset);
      final byte prefix = rightPart.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;

      if ("A".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x7E, (byte) offset};
      } else if ("B".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x46, (byte) offset};
      } else if ("C".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x4E, (byte) offset};
      } else if ("D".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x56, (byte) offset};
      } else if ("E".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x5E, (byte) offset};
      } else if ("H".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x66, (byte) offset};
      } else if ("L".equals(leftPart)) {
        result = new byte[] {prefix, (byte) 0x6E, (byte) offset};
      }

      Assert.assertNotNull("The left part must be A,B,C,D,E,H or L [" + leftPart + ']', result);
    } else if (isRegister16Name(leftPart) || ("A".equals(leftPart) && isInBrakes(rightPart))) {
      final int address = new LightExpression(context, this, asm, extractCalculatedPart(rightPart)).calculate();

      final byte lowByte = (byte) address;
      final byte highByte = (byte) (address >>> 8);

      if (isInBrakes(rightPart)) {
        Assert.assertAddress(address);
        if ("A".equals(leftPart)) {
          result = new byte[] {(byte) 0x3A, lowByte, highByte};
        } else if ("BC".equals(leftPart)) {
          result = new byte[] {(byte) 0xED, (byte) 0x4B, lowByte, highByte};
        } else if ("DE".equals(leftPart)) {
          result = new byte[] {(byte) 0xED, (byte) 0x5B, lowByte, highByte};
        } else if ("HL".equals(leftPart)) {
          result = new byte[] {(byte) 0x2A, lowByte, highByte};
        } else if ("IX".equals(leftPart)) {
          result = new byte[] {(byte) 0xDD, (byte) 0x2A, lowByte, highByte};
        } else if ("IY".equals(leftPart)) {
          result = new byte[] {(byte) 0xFD, (byte) 0x2A, lowByte, highByte};
        } else if ("(HL)".equals(leftPart)) {
          Assert.assertUnsignedByte(address);
          result = new byte[] {(byte) 0x36, lowByte};
        } else if ("SP".equals(leftPart)) {
          result = new byte[] {(byte) 0xED, (byte) 0x7B, lowByte, highByte};
        }
        Assert.assertNotNull("The left part must be A,BC,DE,HL,IX,IY,(HL) or SP [" + leftPart + ']', result);
      } else {
        if (address < 0) {
          Assert.assertSignedShort(address);
        } else {
          Assert.assertAddress(address);
        }

        if ("BC".equals(leftPart)) {
          result = new byte[] {(byte) 0x01, lowByte, highByte};
        } else if ("DE".equals(leftPart)) {
          result = new byte[] {(byte) 0x11, lowByte, highByte};
        } else if ("HL".equals(leftPart)) {
          result = new byte[] {(byte) 0x21, lowByte, highByte};
        } else if ("IX".equals(leftPart)) {
          result = new byte[] {(byte) 0xDD, (byte) 0x21, lowByte, highByte};
        } else if ("IY".equals(leftPart)) {
          result = new byte[] {(byte) 0xFD, (byte) 0x21, lowByte, highByte};
        } else if ("SP".equals(leftPart)) {
          result = new byte[] {(byte) 0x31, lowByte, highByte};
        }

        Assert.assertNotNull("The left part must be BC,DE,HL,IX,IY or SP [" + leftPart + ']', result);
      }
    } else {
      final int value = new LightExpression(context, this, asm, extractCalculatedPart(rightPart)).calculate();
      Assert.assertUnsignedByte(value);
      final byte valueByte = (byte) value;
      if ("(HL)".equals(leftPart)) {
        result = new byte[] {(byte) 0x36, valueByte};
      } else if ("B".equals(leftPart)) {
        result = new byte[] {(byte) 0x06, valueByte};
      } else if ("C".equals(leftPart)) {
        result = new byte[] {(byte) 0x0E, valueByte};
      } else if ("D".equals(leftPart)) {
        result = new byte[] {(byte) 0x16, valueByte};
      } else if ("E".equals(leftPart)) {
        result = new byte[] {(byte) 0x1E, valueByte};
      } else if ("H".equals(leftPart)) {
        result = new byte[] {(byte) 0x26, valueByte};
      } else if ("L".equals(leftPart)) {
        result = new byte[] {(byte) 0x2E, valueByte};
      } else if ("A".equals(leftPart)) {
        result = new byte[] {(byte) 0x3E, valueByte};
      } else if ("(SP)".equals(leftPart)) {
        result = new byte[] {(byte) 0x36, valueByte};
      }

      Assert.assertNotNull("The left part must be A,(HL),B,C,D,E,H,L or (SP) [" + leftPart + ']', result);
    }
    return result;
  }

  private byte[] getMachineCodeWhenRightRegister(final AsmTranslator context, final ParsedAsmLine asm, final String leftPart, final String rightPart) {
    byte[] result = null;

    Assert.assertTrue("The left operand must be in brakes [" + leftPart + ']', isInBrakes(leftPart));
    if (isIndexRegisterReference(leftPart)) {
      final int offset = new LightExpression(context, this, asm, extractCalculatedPart(leftPart)).calculate();
      Assert.assertSignedByte(offset);
      final byte prefix = leftPart.startsWith("(IX") ? (byte) 0xDD : (byte) 0xFD;

      if ("A".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x77, (byte) offset};
      } else if ("B".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x70, (byte) offset};
      } else if ("C".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x71, (byte) offset};
      } else if ("D".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x72, (byte) offset};
      } else if ("E".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x73, (byte) offset};
      } else if ("H".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x74, (byte) offset};
      } else if ("L".equals(rightPart)) {
        result = new byte[] {prefix, (byte) 0x75, (byte) offset};
      }

      Assert.assertNotNull("The right part must be A,B,C,D,E,H or L [" + rightPart + ']', result);
    } else {
      final int address = new LightExpression(context, this, asm, extractCalculatedPart(leftPart)).calculate();
      Assert.assertAddress(address);
      final byte lowByte = (byte) address;
      final byte highByte = (byte) (address >>> 8);

      if ("HL".equals(rightPart)) {
        result = new byte[] {(byte) 0x22, lowByte, highByte};
      } else if ("A".equals(rightPart)) {
        result = new byte[] {(byte) 0x32, lowByte, highByte};
      } else if ("BC".equals(rightPart)) {
        result = new byte[] {(byte) 0xED, (byte) 0x43, lowByte, highByte};
      } else if ("DE".equals(rightPart)) {
        result = new byte[] {(byte) 0xED, (byte) 0x53, lowByte, highByte};
      } else if ("IX".equals(rightPart)) {
        result = new byte[] {(byte) 0xDD, (byte) 0x22, lowByte, highByte};
      } else if ("IY".equals(rightPart)) {
        result = new byte[] {(byte) 0xFD, (byte) 0x22, lowByte, highByte};
      } else if ("SP".equals(rightPart)) {
        result = new byte[] {(byte) 0xED, (byte) 0x73, lowByte, highByte};
      }
      Assert.assertNotNull("The right part must be A,HL,SP,BC,DE,IX or IY [" + rightPart + '[', result);
    }
    return result;
  }

  @Override
  public Arguments getAllowedArgumentsNumber() {
    return Arguments.TWO;
  }
}
