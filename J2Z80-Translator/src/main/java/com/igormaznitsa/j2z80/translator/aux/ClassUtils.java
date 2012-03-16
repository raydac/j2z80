package com.igormaznitsa.j2z80.translator.aux;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.translator.jar.ZJarArchive;
import java.util.*;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ClassGen;

public class ClassUtils {
    private ClassUtils(){
    }

    public static List<Field> findAllFields(final ZJarArchive archive, final ClassGen classGen) {
        final List<Field> result = new ArrayList<Field>();
        _fillAllFields(archive, classGen, result);
        return result;
    }
    
    private static void _fillAllFields(final ZJarArchive archive, final ClassGen classGen, final List<Field> result){
        for (final Field f : classGen.getFields()) {
            if (!f.isStatic()) {
                result.add(f);
            }
        }

        final String superclass = classGen.getSuperclassName();

        if ("java.lang.Object".equals(superclass)) {
            return;
        }

        final ClassGen superClass = archive.findClassForName(superclass);
        if (superClass == null) {
            throw new IllegalArgumentException("Unknown superclass detected [" + classGen.getClassName() + " extends " + superclass + "]");
        }
        _fillAllFields(archive, superClass, result);
    }
    
    public static Set<Class<? extends J2ZAdditionalBlock>> findAllAdditionalBlocksInClass(final Class<?> processingClass) {
        final Set<Class<? extends J2ZAdditionalBlock>> result = new HashSet<Class<? extends J2ZAdditionalBlock>>();
        _findAllAdditionalBlocksInClass(processingClass,result);
        return result;
    }

    private static void _findAllAdditionalBlocksInClass(final Class<?> processor, final Set<Class<? extends J2ZAdditionalBlock>> result) {
        for (final Class<?> type : processor.getInterfaces()) {
            if (J2ZAdditionalBlock.class.isAssignableFrom(type)) {
                result.add(type.asSubclass(J2ZAdditionalBlock.class));
            }
        }

        final Class<?> superclass = processor.getSuperclass();
        if (superclass != Object.class) {
            _findAllAdditionalBlocksInClass(superclass, result);
        }
    }

    public static Set<ClassID> findAllImplementedInterfaces(final ClassContext classContext, final String className) {
        final Set<ClassID> result = new HashSet<ClassID>();

        final ClassGen classGen = classContext.findClassForID(new ClassID(className));

        for (final String interfaceName : classGen.getInterfaceNames()) {
            result.add(new ClassID(interfaceName));
            result.addAll(findAllImplementedInterfaces(classContext, interfaceName));
        }

        return result;
    }


    public static int calculateNeededAreaForClassInstance(final ZJarArchive archive, final ClassGen classGen) {
        if (classGen.isInterface() || classGen.isEnum() || classGen.isAnnotation() || classGen.isAbstract()) {
            return 0;
        }

        final String superClass = classGen.getSuperclassName();

        int fieldNumber = 0;

        for (final Field fld : classGen.getFields()) {
            if (fld.isStatic()) {
                continue;
            }

            switch (fld.getType().getType()) {
                case Constants.T_DOUBLE:
                case Constants.T_FLOAT:
                case Constants.T_LONG:
                    throw new IllegalStateException("Class " + classGen.getClassName() + " contains inappropriate field [" + fld.toString() + ']');
                default: {
                    fieldNumber++;
                }
                break;
            }


            fieldNumber++;
        }

        if ("java.lang.Object".equals(superClass)) {
            return (fieldNumber << 1);
        }

        final ClassGen superClassGen = archive.findClassForName(superClass);
        if (superClassGen == null) {
            throw new IllegalStateException("Not found superclass " + superClass + " for " + classGen.getClassName());
        }

        return (fieldNumber << 1) + calculateNeededAreaForClassInstance(archive,superClassGen);
    }

}
