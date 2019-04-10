package com.igormaznitsa.j2z80.jvmprocessors;

import com.igormaznitsa.j2z80.translator.MethodTranslator;
import com.igormaznitsa.j2z80.utils.Assert;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LDC_W;

import java.io.IOException;
import java.io.Writer;

// class to process LDC_W with code 19
public class Processor_LDC_W extends AbstractJvmCommandProcessor {

  private final String template;

  public Processor_LDC_W() {
    super();
    template = loadResourceFileAsString("LDC_W.a80");
  }

  @Override
  public String getName() {
    return "LDC_W";
  }

  @Override
  public void process(final MethodTranslator methodTranslator, final Instruction instruction, final InstructionHandle handle, final Writer out) throws IOException {
    final LDC_W ldcw = (LDC_W) instruction;

    final int index = ldcw.getIndex();

    String strvalue = null;
    final Constant cp_constant = methodTranslator.getConstantPool().getConstant(index);

    if (cp_constant instanceof ConstantInteger) {
      final ConstantInteger constInt = (ConstantInteger) cp_constant;
      final int value = constInt.getBytes();
      Assert.assertSignedShort(value);
      strvalue = Integer.toString(value);
    } else if (cp_constant instanceof ConstantUtf8 || cp_constant instanceof ConstantString) {
      strvalue = methodTranslator.registerUsedConstantPoolItem(index);
    } else {
      methodTranslator.getTranslatorContext().getLogger().logError("Unsupported constant pool element has been detected [" + cp_constant.toString() + ']');
      throw new IllegalArgumentException("Unsupported constant pool item detected in LDCW instruction [" + cp_constant.toString() + ']');
    }

    out.write(template.replace(MACROS_ADDRESS, strvalue));
    out.write(NEXT_LINE);
  }
}
