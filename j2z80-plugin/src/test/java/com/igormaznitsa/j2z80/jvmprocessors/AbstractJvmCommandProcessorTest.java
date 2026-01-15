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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.z80asm.Z80Asm;
import j80.cpu.Z80;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SuppressWarnings("serial")
public abstract class AbstractJvmCommandProcessorTest extends Z80 {

  public static final int START_ADDRESS = 0x6000;
  public static final int INIT_SP = 0xFFF0;
  public final static int CONSTANT_INT = 35;
  public final static int CONSTANT_UTF8 = 28;
  public final static int CONSTANT_DOUBLE = 32;
  public final static int CONSTANT_LONG = 34;
  public final static int CONSTANT_FLOAT = 37;
  public final static int CONSTANT_STR = 39;
  public final static int CONSTANT_MOCK_METHOD = 100;
  public final static int CONSTANT_USER_DEFINED = 1000;
  public final static String ETALON_CONSTANT_STRING = "HELLO WORLD";
  public final static int ETALON_CONSTANT_INTEGER = -31023;
  public final static double ETALON_CONSTANT_DOUBLE = 22.345d;
  public final static float ETALON_CONSTANT_FLOAT = 3.14f;
  public final static long ETALON_CONSTANT_LONG = 21341231324L;
  public static final String END_LABEL = "_###END###_";
  public final TranslatorContext TRANSLATOR_MOCK = mock(TranslatorContext.class);
  public final ClassContext CLASSCONTEXT_MOCK = mock(ClassContext.class);
  public final MethodContext METHODCONTEXT_MOCK = mock(MethodContext.class);
  public final TranslatorLogger LOGGER_MOCK = mock(TranslatorLogger.class);
  public final ClassGen CLASS_GEN_MOCK = mock(ClassGen.class);
  public final JavaClass JCLASS_GEN_MOCK = mock(JavaClass.class);
  public final ConstantPoolGen CP_GEN_MOCK = mock(ConstantPoolGen.class);
  public final ConstantPool CP_MOCK = mock(ConstantPool.class);
  public final MethodTranslator CLASS_PROCESSOR_MOCK = mock(MethodTranslator.class);
  protected final byte[] memory = new byte[0xFFFF];
  protected final String MOCK_CLASS_NAME = "some.mock_class";
  public MethodGen mockupOfInvokedMethod = null;
  protected int endAddress = 0x0000;
  protected Map<String, Integer> breakpointAddresses = new HashMap<String, Integer>();
  protected ClassMethodInfo CLASSMETHOD_INFO;
  private boolean endAddressMet = false;

  public AbstractJvmCommandProcessorTest() {
    super();

    when(JCLASS_GEN_MOCK.getClassName()).thenReturn(MOCK_CLASS_NAME);

    when(CLASS_GEN_MOCK.getConstantPool()).thenReturn(CP_GEN_MOCK);
    when(CLASS_GEN_MOCK.getClassName()).thenReturn(MOCK_CLASS_NAME);
    when(CLASS_GEN_MOCK.getJavaClass()).thenReturn(JCLASS_GEN_MOCK);

    when(CLASS_PROCESSOR_MOCK.getTranslatorContext()).thenReturn(TRANSLATOR_MOCK);

    when(TRANSLATOR_MOCK.getClassContext()).thenReturn(CLASSCONTEXT_MOCK);
    when(TRANSLATOR_MOCK.getMethodContext()).thenReturn(METHODCONTEXT_MOCK);
    when(TRANSLATOR_MOCK.getLogger()).thenReturn(LOGGER_MOCK);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        System.err.println("ERROR: " + invocation.getArguments()[0]);
        return null;
      }
    }).when(LOGGER_MOCK).logError(anyString());

    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(final InvocationOnMock invocation) throws Throwable {
        System.out.println("INFO: " + invocation.getArguments()[0]);
        return null;
      }
    }).when(LOGGER_MOCK).logInfo(anyString());

    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(final InvocationOnMock invocation) throws Throwable {
        System.err.println("WARNING: " + invocation.getArguments()[0]);
        return null;
      }
    }).when(LOGGER_MOCK).logWarning(anyString());

    // mock ConstantPool
    when(CP_GEN_MOCK.getConstant(CONSTANT_INT)).thenReturn(new ConstantInteger(ETALON_CONSTANT_INTEGER));
    when(CP_GEN_MOCK.getConstant(CONSTANT_UTF8)).thenReturn(new ConstantUtf8(ETALON_CONSTANT_STRING));
    when(CP_GEN_MOCK.getConstant(CONSTANT_FLOAT)).thenReturn(new ConstantFloat(ETALON_CONSTANT_FLOAT));
    when(CP_GEN_MOCK.getConstant(CONSTANT_LONG)).thenReturn(new ConstantLong(ETALON_CONSTANT_LONG));
    when(CP_GEN_MOCK.getConstant(CONSTANT_DOUBLE)).thenReturn(new ConstantDouble(ETALON_CONSTANT_DOUBLE));
    when(CP_GEN_MOCK.getConstant(CONSTANT_STR)).thenReturn(new ConstantString(CONSTANT_UTF8));

    when(CP_GEN_MOCK.getConstantPool()).thenReturn(CP_MOCK);

    when(CLASS_PROCESSOR_MOCK.getConstantPool()).thenReturn(CP_GEN_MOCK);

    when(CP_MOCK.getConstant(anyInt())).thenAnswer(new Answer<Constant>() {

      @Override
      public Constant answer(final InvocationOnMock invocation) throws Throwable {
        final int index = ((Integer) invocation.getArguments()[0]).intValue();
        return CP_GEN_MOCK.getConstant(index);
      }
    });

    mockupOfInvokedMethod = makeMethodMockup();

    CLASSMETHOD_INFO = new ClassMethodInfo(CLASS_GEN_MOCK, mockupOfInvokedMethod.getMethod(), mockupOfInvokedMethod);

    when(CLASS_PROCESSOR_MOCK.getMethod()).thenReturn(CLASSMETHOD_INFO);
    when(CLASS_PROCESSOR_MOCK.getConstantPool()).thenReturn(CP_GEN_MOCK);
    when(CLASS_PROCESSOR_MOCK.getTranslatorContext()).thenReturn(TRANSLATOR_MOCK);
  }

  protected MethodGen makeMethodMockup() {
    return new MethodGen(Const.ACC_STATIC, Type.VOID, new Type[0], new String[0], "MOCK_METHOD", MOCK_CLASS_NAME, new InstructionList(), CP_GEN_MOCK);
  }

  protected void registerBreakPoint(final String labelName) {
    breakpointAddresses.put(labelName, Integer.valueOf(0));
  }

  private Z80Asm processAsm(final String asm) {
    final String[] asmArray = new String[] {
        getAsmBeforeAll(),
        " org #" + Integer.toHexString(START_ADDRESS),
        getAsmPrefix(),
        asm,
        getAsmPostfix(),
        END_LABEL + ": NOP"
    };

    for (final String str : asmArray) {
      System.out.println(str);
    }

    final Z80Asm z80asm = new Z80Asm(Arrays.asList(asmArray));
    final byte[] bindata = z80asm.process();
    endAddress = z80asm.findLabelAddress(END_LABEL).intValue();

    if (!breakpointAddresses.isEmpty()) {
      // fill breakpoint addresses
      final Map<String, Integer> result = new HashMap<String, Integer>();
      for (final String labelName : breakpointAddresses.keySet()) {
        final Integer address = z80asm.findLabelAddress(labelName);
        assertNotNull("Breakpoint label " + labelName + "is not found", address);
        result.put(labelName, Integer.valueOf(address.intValue()));
      }
      breakpointAddresses.clear();
      breakpointAddresses.putAll(result);
    }

    System.arraycopy(bindata, 0, memory, z80asm.getDataOffset(), bindata.length);
    PC = z80asm.getDataOffset();

    endAddressMet = false;
    exec(Integer.MAX_VALUE);
    assertTrue("The end address is not met", endAddressMet);

    return z80asm;
  }

  protected MethodGen registerMockMethod(final int index, final String className, final String methodName, final int accessFlags, final int maxLocals, final Type[] argTypes, final Type returnType) {
    final int INDEX_CLASS_NAME = index + 1;
    final int INDEX_METHOD_NAME = index + 2;
    final int INDEX_METHOD_SIGNATURE = index + 3;
    final int INDEX_CLASS = index + 4;
    final int INDEX_NAME_TYPE = index + 5;

    final ConstantUtf8 cpClassName = new ConstantUtf8(className);
    final ConstantUtf8 cpMethodName = new ConstantUtf8(methodName);
    final ConstantUtf8 cpMethodSignature = new ConstantUtf8(Type.getMethodSignature(returnType, argTypes));

    final ConstantClass cpClass = new ConstantClass(INDEX_CLASS_NAME);
    final ConstantNameAndType cpNameType = new ConstantNameAndType(INDEX_METHOD_NAME, INDEX_METHOD_SIGNATURE);

    final ConstantMethodref methodRef = new ConstantMethodref(INDEX_CLASS, INDEX_NAME_TYPE);

    when(CP_GEN_MOCK.getConstant(INDEX_CLASS_NAME)).thenReturn(cpClassName);
    when(CP_GEN_MOCK.getConstant(INDEX_METHOD_NAME)).thenReturn(cpMethodName);
    when(CP_GEN_MOCK.getConstant(INDEX_METHOD_SIGNATURE)).thenReturn(cpMethodSignature);
    when(CP_GEN_MOCK.getConstant(INDEX_NAME_TYPE)).thenReturn(cpNameType);
    when(CP_GEN_MOCK.getConstant(index)).thenReturn(methodRef);
    when(CP_MOCK.getConstantString(INDEX_CLASS, Const.CONSTANT_Class)).thenReturn(className);

    final String[] argNames = new String[argTypes.length];
    for (int i = 0; i < argTypes.length; i++) {
      argNames[i] = "a" + i;
    }

    final MethodGen fakeMethod = new MethodGen(accessFlags, returnType, argTypes, argNames, methodName, className, new InstructionList(), CP_GEN_MOCK);

    when(METHODCONTEXT_MOCK.findMethod(eq(new MethodID(fakeMethod)))).thenReturn(fakeMethod);
    return fakeMethod;
  }

  @Override
  public boolean step() {
    endAddressMet = endAddress == PC;

    if (!endAddressMet && !breakpointAddresses.isEmpty()) {
      // process breakpoints
      for (final Map.Entry<String, Integer> entry : breakpointAddresses.entrySet()) {
        if (entry.getValue().intValue() == PC) {
          processBreakPoint(entry.getKey());
        }
      }
    }


    return endAddressMet;
  }

  public void processBreakPoint(final String label) {
  }

  public Z80Asm assertLinearExecutionToEnd(final String asm) {
    return processAsm(asm);
  }

  @Before
  public void beforeTest() {
    reset();
    SP = INIT_SP;
    breakpointAddresses.clear();
  }

  @Override
  public int getByteFromMemory(int address) {
    return memory[address] & 0xFF;
  }

  @Override
  public void setByteToMemory(int address, int value) {
    memory[address] = (byte) value;
  }

  public void assertElementsOnStack(final int numberElementsOnStack) {
    Assert.assertEquals("Stack must contain " + numberElementsOnStack + " element(s)", numberElementsOnStack, (INIT_SP - SP) >> 1);
  }

  public void assertStackEmpty() {
    org.junit.Assert.assertEquals("Stack must be empty", INIT_SP, SP);
  }

  public String getAsmPostfix() {
    return "";
  }

  public String getAsmPrefix() {
    return "";
  }

  public String getAsmBeforeAll() {
    return "";
  }

  protected int readLocalFrameVariable(final int index) {
    final int offset = index << 1;
    return (peekb(IX - offset + 1) << 8) | peekb(IX - offset);
  }

  protected void writeLocalFrameVariable(final int index, final int value) {
    final int offset = index << 1;
    pokeb(IX - offset + 1, value >>> 8);
    pokeb(IX - offset, value);
  }
}
