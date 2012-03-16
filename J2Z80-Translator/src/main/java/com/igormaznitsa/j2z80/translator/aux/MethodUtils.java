package com.igormaznitsa.j2z80.translator.aux;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

public class MethodUtils {
    private MethodUtils(){
    }
    
    public static boolean isStaticInitializer(final Method method) {
        return method.isStatic() && method.getName().equals("<clinit>") 
                && method.getArgumentTypes().length == 0 
                && method.getReturnType().getType() == Type.VOID.getType();
    }
   
    
}
