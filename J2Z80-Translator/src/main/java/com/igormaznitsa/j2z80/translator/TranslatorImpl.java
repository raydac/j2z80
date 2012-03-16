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
package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.*;
import com.igormaznitsa.j2z80.api.additional.*;
import com.igormaznitsa.j2z80.aux.*;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import com.igormaznitsa.j2z80.ids.*;
import com.igormaznitsa.j2z80.jvmprocessors.*;
import com.igormaznitsa.j2z80.translator.aux.*;
import com.igormaznitsa.j2z80.translator.jar.ZJarArchive;
import java.io.*;
import java.util.Map.Entry;
import java.util.*;
import java.util.jar.JarEntry;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

@SuppressWarnings("serial")
public class TranslatorImpl implements TranslatorContext{

    final ZJarArchive workingArchive;

    private final ClassContextImpl classContext = new ClassContextImpl(this);
    private final MethodContextImpl methodContext = new MethodContextImpl(this);
    
    private final TranslatorLogger messageLogger;
    private final List<MethodID> methodsToBeProcessed;
    private final Set<MethodID> methodsUsedInInvokeinterface = new HashSet<MethodID>();
    private final Map<ClassMethodInfo, String[]> asmForMethods = new LinkedHashMap<ClassMethodInfo, String[]>();
    private final Set<Class<? extends J2ZAdditionalBlock>> registeredAdditions = new HashSet<Class<? extends J2ZAdditionalBlock>>();
    private final Set<AbstractBootClass> usedBootstrapClassese = new HashSet<AbstractBootClass>();
    private final Map<String, Constant> classPoolConstants = new HashMap<String, Constant>();
    private final Set<ClassID> classesForCheckcast = new HashSet<ClassID>();

    public ClassGen findOverridenMethodOnPath(final String className, final String superClassName, final MethodGen method) {
        ClassGen classGen = workingArchive.findClassForName(className);
        while (classGen != null) {
            if (superClassName.equals(classGen.getClassName())) {
                return null;
            }
            for (final Method meth : classGen.getMethods()) {
                if (meth.getName().equals(method.getName()) && meth.getReturnType().equals(method.getReturnType()) && Arrays.deepEquals(meth.getArgumentTypes(), method.getArgumentTypes())) {
                    return classGen;
                }
            }
            classGen = workingArchive.findClassForName(classGen.getSuperclassName());
        }
        return null;
    }

    public TranslatorImpl(final File jarFile, final TranslatorLogger logger) throws IOException {
        this.messageLogger = logger == null ? new DefaultTranslatorLogger() : logger;
        workingArchive = new ZJarArchive(jarFile, Z80_MAIN_METHOD_NAME, Z80_MAIN_METHOD_SIGNATURE);

        if (workingArchive.getClasses().isEmpty()) {
            throw new IllegalArgumentException("The archive doesn't contain any class");
        }

        if (workingArchive.getMainMethods().isEmpty()) {
            throw new IllegalArgumentException("There is not any main-class in the archive");
        }

        classContext.init();
        methodsToBeProcessed = Collections.unmodifiableList(methodContext.init());
    }

 
    private void reset() {
        methodsUsedInInvokeinterface.clear();
        asmForMethods.clear();
        registeredAdditions.clear();
        usedBootstrapClassese.clear();
        classPoolConstants.clear();
        classesForCheckcast.clear();
    }

     @Override
    public void registerAdditionsUsedByClass(final Class<?> classToCheck) {
        for (final Class<? extends J2ZAdditionalBlock> l : ClassUtils.findAllAdditionalBlocksInClass(classToCheck)) {
            registeredAdditions.add(l);
        }
    }

    @Override
    public String[] translate(final String mainClassName, final int startAddress, final int stackTop) throws IOException {
        reset();

        Assert.assertAddress(startAddress);
        Assert.assertAddress(stackTop);
        
        getLogger().logInfo("The Start address for translation :"+Utils.intToString(startAddress));
        getLogger().logInfo("The Stack Top address :"+Utils.intToString(stackTop));
        
        
        final MethodID mainMethodID = findMainMethod(mainClassName);

        final ClassMethodInfo mainMethod = methodContext.findMethodInfo(mainMethodID);

        final String[] mainMethodAsm = translateMethod(mainMethodID);

        final MainPrefixPostfixGenerator prefixPostfixGenerator = new MainPrefixPostfixGenerator(mainMethod, startAddress, stackTop);

        final String[] staticInitBlocks = processStaticInitializingBlocks();

        asmForMethods.put(mainMethod, Utils.concatStringArrays(prefixPostfixGenerator.generatePrefix(), staticInitBlocks, mainMethodAsm, prefixPostfixGenerator.generatePostfix()));

        for (final MethodID methodToProcess : methodsToBeProcessed) {
            if (!mainMethodID.equals(methodToProcess)) {
                translateMethod(methodToProcess);
            }
        }

        final List<String> result = new ArrayList<String>();
        for (final String[] text : asmForMethods.values()) {
            for (final String str : text) {
                final String trimmed = str.trim();
                if (trimmed.isEmpty()) {
                    continue; // || trimmed.charAt(0) == ';') continue;
                }
                result.add(str);
            }
        }

        processUsedBootstrapClasses(result);
        processClassFields(result);
        processConstantPool(result);
        processJNIClasses(result);
        processIDs(result);
        processAdditionals(result);

        result.addAll(makeClassSizeArray());

        return result.toArray(new String[result.size()]);
    }

    private String[] processStaticInitializingBlocks() {
        getLogger().logInfo("----PROCESS STATIC INITIALIZERS ----");
        final List<ClassID> classesContainStaticInitializing = new ArrayList<ClassID>();
        for (final Entry<ClassID, ClassMethodInfo> id : classContext.getAllRegisteredClasses()) {
            final ClassGen cgen = id.getValue().getClassInfo();
            for (final Method m : cgen.getMethods()) {
                if (MethodUtils.isStaticInitializer(m)) {
                    classesContainStaticInitializing.add(id.getKey());
                }
            }
        }

        getLogger().logInfo("Static initialization method has been detected in " + classesContainStaticInitializing.size() + " class(es)");

        if (classesContainStaticInitializing.isEmpty()) {
            return new String[0];
        }

        Collections.sort(classesContainStaticInitializing, new Comparator<ClassID>() {

            @Override
            public int compare(final ClassID arg0, final ClassID arg1) {
                if (arg0.equals(arg1)) {
                    return 0;
                }

                if (classContext.isAccessible(classContext.findClassForID(arg1), classContext.findClassForID(arg0).getClassName())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        final Processor_INVOKESTATIC invokeStaticProc = (Processor_INVOKESTATIC) AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

        String[] result = new String[]{";------ STATIC INITIALIZING BLOCK ------"};

        for (final ClassID id : classesContainStaticInitializing) {
            final ClassGen cgen = classContext.findClassForID(id);
            result = Utils.concatStringArrays(result, invokeStaticProc.generateCallForStaticInitalizer(cgen));
        }

        return result;
    }

    private void processIDs(final List<String> out) {
        // class id
        for (final Entry<ClassID, ClassMethodInfo> id : classContext.getAllRegisteredClasses()) {
            out.add(LabelUtils.makeLabelForClassID(id.getKey()) + ": EQU " + id.getValue().getUID());
        }

        // method id
        for (final Entry<MethodID, ClassMethodInfo> id : methodContext.getMethods()) {
            out.add(LabelUtils.makeLabelForMethodID(id.getKey()) + ": EQU " + id.getValue().getUID());
        }
    }

    private void processClassFields(final List<String> out) {
        for (final ClassGen currentClass : workingArchive.getClasses().values()) {
            final List<Field> fieldList = ClassUtils.findAllFields(workingArchive,currentClass);
            final String className = currentClass.getClassName();
            int offset = 0;
            for (final Field f : fieldList) {
                out.add(LabelUtils.makeLabelNameForFieldOffset(className, f.getName(), f.getType()) + ": EQU " + offset);
                offset += 2;
            }

            // reservation cells for static fields
            for (final Field f : currentClass.getFields()) {
                if (f.isStatic()) {
                    out.add(LabelUtils.makeLabelNameForField(className, f.getName(), f.getType()) + ": DEFW 0");
                }
            }
        }
    }

    private void processConstantPool(final List<String> out) {
        for (final Entry<String, Constant> c : classPoolConstants.entrySet()) {
            final String label = c.getKey();
            final Constant constant = c.getValue();
            if (constant instanceof ConstantInteger) {
                out.add(label + ": DEFW #" + Integer.toHexString(((ConstantInteger) constant).getBytes() & 0xFFFF).toUpperCase(Locale.ENGLISH));
            } else if (constant instanceof ConstantUtf8) {
                final String str = ((ConstantUtf8) constant).getBytes();
                out.add(label + ": DEFB " + str.length());
                out.add("DEFM \"" + str + '\"');
            }
        }
    }

    private void processUsedBootstrapClasses(final List<String> out) {
        for (final AbstractBootClass processor : usedBootstrapClassese) {
            final String[] text = processor.getAdditionalText();
            for (final String s : text) {
                out.add(s);
            }
        }
    }

    private List<String> makeClassSizeArray() {
        final List<String> result = new ArrayList<String>();
        for (final ClassGen curClass : workingArchive.getClasses().values()) {
            if (curClass.isAbstract() || curClass.isInterface()) {
                continue;
            }
            result.add(LabelUtils.makeLabelForClassSizeInfo(curClass.getClassName()) + ": EQU " + ClassUtils.calculateNeededAreaForClassInstance(workingArchive,curClass));
        }

        return result;
    }


    private void processJNIClasses(final List<String> text) throws IOException {
        getLogger().logInfo("----PROCESS JNI CLASSES----");
        getLogger().logInfo("Detected " + classContext.getClassesWithDetectedJNI().size() + " class(es) contain(s) JNI methods");
        final NativeClassProcessor processor = new NativeClassProcessor(this);
        for (final ClassID classInfo : classContext.getClassesWithDetectedJNI()) {
            getLogger().logInfo("Process " + classInfo);
            for (final String str : processor.findNativeSources(classContext.findClassInfoForID(classInfo))) {
                text.add(str);
            }
        }
    }

    private void processAdditionals(final List<String> text) throws IOException {
        getLogger().logInfo("----PROCESS ADDITIONS----");
        boolean needMemoryManager = false;
        for (final Class<? extends J2ZAdditionalBlock> addition : registeredAdditions) {
            if (addition == NeedsMemoryManager.class) {
                // memory manager must be in the end of code
                needMemoryManager = true;
                continue;
            }

            getLogger().logInfo("Detected addition usage " + addition.getSimpleName());

            final AdditionPath path = addition.getAnnotation(AdditionPath.class);
            if (path == null) {
                throw new IllegalStateException("Detected addition without path [" + addition.getCanonicalName() + ']');
            }

            final String assemblerText = Utils.readTextResource(AbstractJvmCommandProcessor.class, path.value());
            for (final String str : Utils.breakToLines(preprocessAdditionAssemblerText(addition, assemblerText))) {
                text.add(str);
            }
        }

        if (needMemoryManager) {
            getLogger().logInfo("Detected addition usage " + NeedsMemoryManager.class.getSimpleName());
            final String assemblerText = Utils.readTextResource(AbstractJvmCommandProcessor.class, (NeedsMemoryManager.class.getAnnotation(AdditionPath.class)).value());
            for (final String str : Utils.breakToLines(preprocessAdditionAssemblerText(NeedsMemoryManager.class, assemblerText))) {
                text.add(str);
            }
        }
    }

    private String preprocessAdditionAssemblerText(final Class<? extends J2ZAdditionalBlock> addition, final String text) {
        String result = text;
        if (addition == NeedsINVOKEINTERFACEManager.class) {
            result = text.replace(NeedsINVOKEINTERFACEManager.MACROS_INVOKEINTERFACE_TABLE, prepareInvokeinterfaceMacrosContent());
        } else if (addition == NeedsINVOKEVIRTUALManager.class) {
            final InvokevirtualTable table = new InvokevirtualTable(this);
            result = text.replace(NeedsINVOKEVIRTUALManager.MACROS_INVOKEVIRTUAL_TABLE, table.toAsm());
        } else if (addition == NeedsInstanceofManager.class) {
            final InstanceofTable table = new InstanceofTable(this, classesForCheckcast);
            result = text.replace(NeedsInstanceofManager.MACRO_INSTANCEOFTABLE, table.toAsm());
        }

        return result;
    }

    private String prepareInvokeinterfaceMacrosContent() {
        final InvokeinterfaceTable table = new InvokeinterfaceTable(this, methodsUsedInInvokeinterface);
        return table.generateAsm();
    }

    private String[] translateMethod(final MethodID methodId) throws IOException {
        final ClassMethodInfo method = methodContext.findMethodInfo(methodId);

        String[] asmForTheMethod = null;
        try {
            if (!method.isNative()) {
                asmForTheMethod = new MethodTranslator(this, method).translate();
                asmForMethods.put(method, asmForTheMethod);
            }

        } catch (Exception ex) {
            getLogger().logError("Exception during " + methodId.toString() + " [" + ex + ']');
            throw new IOException("Can't translate " + methodId, ex);
        }
        return asmForTheMethod;
    }

    private MethodID findMainMethod(final String mainClassName) {
        MethodID result = null;

        final Map<ClassGen, Method> mainMethods = workingArchive.getMainMethods();
        getLogger().logInfo("----FOUND MAIN METHODS---");
        for (final Entry<ClassGen, Method> m : mainMethods.entrySet()) {
            getLogger().logInfo("Main method at " + m.getKey().getClassName());
        }

        if (mainClassName == null) {
            if (workingArchive.getMainMethods().size() == 1) {
                for (final Entry<ClassGen, Method> entry : workingArchive.getMainMethods().entrySet()) {
                    result = new MethodID(entry.getKey(), entry.getValue());
                }
            } else {
                throw new IllegalStateException("The archive contains more than one main class so it must be defined explicitly");
            }
        } else {
            for (final Entry<ClassGen, Method> entry : workingArchive.getMainMethods().entrySet()) {
                if (mainClassName.equals(entry.getKey().getClassName())) {
                    result = new MethodID(entry.getKey(), entry.getValue());
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Can't find any main class for the name [" + mainClassName + ']');
            }
        }
        return result;
    }

    @Override
    public Integer registerClassForCastCheck(final ClassID classId) {
        classesForCheckcast.add(classId);
        return classContext.findClassUID(classId);
    }

    @Override
    public Integer registerInterfaceMethodForINVOKEINTERFACE(final MethodID methodId) {
        methodsUsedInInvokeinterface.add(methodId);
        return methodContext.findMethodUID(methodId);
    }

    @Override
    public void registerConstantPoolItem(final String constantLabel, final Constant item) {
        this.classPoolConstants.put(constantLabel, item);
    }

    @Override
    public void registerCalledBootClassProcesser(AbstractBootClass bootClass) {
        Assert.assertNotNull("Boot class must not be null", bootClass);
        usedBootstrapClassese.add(bootClass);
    }

    @Override
    public TranslatorLogger getLogger() {
        return messageLogger;
    }

    @Override
    public byte[] loadResourceForPath(final String path) throws IOException {
        final String normalizePath = path.replace('\\', '/');
        final JarEntry entry = workingArchive.findEntryForPath(normalizePath);
        if (entry != null && !entry.isDirectory()) {
            return workingArchive.extractEntry(entry);
        }
        return null;
    }

    @Override
    public ClassContext getClassContext(){
        return classContext;
    }
    
    @Override
    public MethodContext getMethodContext(){
        return methodContext;
    }
}
