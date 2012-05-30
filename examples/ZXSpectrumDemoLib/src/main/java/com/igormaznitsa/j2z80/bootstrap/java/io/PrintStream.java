package com.igormaznitsa.j2z80.bootstrap.java.io;

import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import java.io.IOException;
import org.apache.bcel.generic.Type;

public class PrintStream extends AbstractBootClass {

    @Override
    public String[] generateInvocation(final TranslatorContext translator, final String methodName, final Type[] methodArguments, final Type resultType) {
        if (methodName.equals("println")) {
            return new String[]{"CALL JAVA.LANG.SYSTEN.PRINTLN"};
        } else {
            throwBootClassExceptionForMethod(methodName, resultType, methodArguments);
            return null;
        }
    }

    @Override
    public boolean doesInvokeNeedFrame(TranslatorContext translator, String methodName, Type[] methodArguments, Type resultType) {
        return true;
    }

    @Override
    public String[] generateFieldSetter(final TranslatorContext translator, final String fieldName, final Type fieldType, final boolean isStatic) {
        throwBootClassExceptionForField(fieldName, fieldType);
        return null;
    }

    @Override
    public String[] generateFieldGetter(final TranslatorContext translator, final String fieldName, final Type fieldType, final boolean isStatic) {
        throwBootClassExceptionForField(fieldName, fieldType);
        return null;
    }

    @Override
    public String[] getAdditionalText() {
        final String fileName = this.getClass().getSimpleName() + ".a80";
        try{
            return Utils.breakToLines(Utils.readTextResource(this.getClass(), fileName));
        } catch (IOException ex) {
            throw new RuntimeException("IOException [" + fileName + ']', ex);
        }
    }
}
