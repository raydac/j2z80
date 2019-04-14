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

package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.api.additional.AdditionPath;
import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEINTERFACEManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEVIRTUALManager;
import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_INVOKESTATIC;
import com.igormaznitsa.j2z80.translator.jar.ZClassPath;
import com.igormaznitsa.j2z80.translator.jar.ZParsedJar;
import com.igormaznitsa.j2z80.translator.optimizator.AsmOptimizerChain;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationChainFactory;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.j2z80.translator.utils.AsmAssertions;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import com.igormaznitsa.j2z80.translator.utils.MethodUtils;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The class is the Translator implementation. It is the core central class which implements the main work-flow.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class TranslatorImpl implements TranslatorContext {

  final ZClassPath workingClassPath;

  private final ClassContextImpl classContext = new ClassContextImpl(this);
  private final MethodContextImpl methodContext = new MethodContextImpl(this);

  private final TranslatorLogger messageLogger;
  private final List<MethodID> methodsToBeProcessed;
  private final Set<MethodID> methodsUsedInInvokeinterface = new HashSet<>();
  private final Map<ClassMethodInfo, String[]> asmForMethods = new LinkedHashMap<>();
  private final Set<Class<? extends J2ZAdditionalBlock>> registeredAdditions = new HashSet<>();
  private final Set<AbstractBootClass> usedBootstrapClassese = new HashSet<>();
  private final Map<String, Constant> classPoolConstants = new HashMap<>();
  private final Set<ClassID> classesForCheckcast = new HashSet<>();
  private String[] excludeResourcePatterns;
  private OptimizationLevel optimizationLevel;

  public TranslatorImpl(final TranslatorLogger logger, final OptimizationLevel optimization, final File... jarArchives) throws IOException {
    this.optimizationLevel = optimization;
    this.messageLogger = logger == null ? new DefaultTranslatorLogger() : logger;

    workingClassPath = new ZClassPath(this, parseJars(jarArchives));

    if (workingClassPath.getAllClasses().isEmpty()) {
      throw new IllegalArgumentException("The Class path doesn't have any class");
    }

    classContext.init();
    methodsToBeProcessed = Collections.unmodifiableList(methodContext.init());
  }

  private static List<ParsedAsmLine> asParsedLines(final List<String> list) {
    if (list == null) {
      return new ArrayList<>(0);
    }
    final List<ParsedAsmLine> result = new ArrayList<>(list.size());
    for (final String str : list) {
      final ParsedAsmLine line = new ParsedAsmLine(str);
      if (line.isEmpty()) {
        continue;
      }
      result.add(line);
    }
    return result;
  }

  private static List<String> asStringLines(final List<ParsedAsmLine> list) {
    if (list == null) {
      return new ArrayList<>(0);
    }
    final List<String> result = new ArrayList<>(list.size());
    for (final ParsedAsmLine asm : list) {
      result.add(asm.toString());
    }
    return result;
  }

  public ClassGen findOverridenMethodOnPath(final String className, final String superClassName, final MethodGen method) {
    ClassGen classGen = workingClassPath.findClassForName(className);
    while (classGen != null) {
      if (superClassName.equals(classGen.getClassName())) {
        return null;
      }
      for (final Method meth : classGen.getMethods()) {
        if (meth.getName().equals(method.getName()) && meth.getReturnType().equals(method.getReturnType()) && Arrays.deepEquals(meth.getArgumentTypes(), method.getArgumentTypes())) {
          return classGen;
        }
      }
      classGen = workingClassPath.findClassForName(classGen.getSuperclassName());
    }
    return null;
  }

  private ZParsedJar[] parseJars(final File... jarFiles) throws IOException {
    final ZParsedJar[] result = new ZParsedJar[jarFiles.length];

    int index = 0;
    for (final File jarFile : jarFiles) {
      result[index++] = new ZParsedJar(jarFile);
    }

    return result;
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
    registeredAdditions.addAll(ClassUtils.findAllAdditionalBlocksInClass(classToCheck));
  }

  @Override
  public String[] translate(final String mainClassName, final int startAddress, final int stackTop, final String[] patternsExcludeBinResources) throws IOException {
    reset();

    AsmAssertions.assertAddress(startAddress);
    AsmAssertions.assertAddress(stackTop);

    getLogger().logInfo("The Start address for translation :" + Utils.intToString(startAddress));
    getLogger().logInfo("The Stack Top address :" + Utils.intToString(stackTop));


    this.excludeResourcePatterns = patternsExcludeBinResources;

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

    final List<String> result = new ArrayList<>();
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
    processBinaryData(result);
    processAdditionals(result);

    result.addAll(makeClassSizeArray());

    if (optimizationLevel != null && optimizationLevel != OptimizationLevel.NONE) {
      getLogger().logWarning("Make optimization for the level \'" + optimizationLevel.getTextName() + "\'");

      final List<ParsedAsmLine> asmLines = asParsedLines(result);
      final AsmOptimizerChain chain = OptimizationChainFactory.getOptimizators(this, optimizationLevel);
      final List<String> optimizedAsString = asStringLines(chain.processSources(asmLines));

      optimizedAsString.add(0, "; optimization level is \'" + optimizationLevel.getTextName() + '\'');

      return optimizedAsString.toArray(new String[0]);
    } else {
      getLogger().logInfo("No optimization");

      return result.toArray(new String[0]);
    }
  }

  private String[] processStaticInitializingBlocks() {
    getLogger().logInfo("----PROCESS STATIC INITIALIZERS ----");
    final List<ClassID> classesContainStaticInitializing = new ArrayList<>();
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

    classesContainStaticInitializing.sort((arg0, arg1) -> {
      if (arg0.equals(arg1)) {
        return 0;
      }

      if (classContext.isAccessible(classContext.findClassForID(arg1), classContext.findClassForID(arg0).getClassName())) {
        return 1;
      } else {
        return -1;
      }
    });

    final Processor_INVOKESTATIC invokeStaticProc = (Processor_INVOKESTATIC) AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

    String[] result = new String[] {";------ STATIC INITIALIZING BLOCK ------"};

    for (final ClassID id : classesContainStaticInitializing) {
      final ClassGen cgen = classContext.findClassForID(id);
      result = Utils.concatStringArrays(result, invokeStaticProc.generateCallForStaticInitalizer(cgen));
    }

    return result;
  }

  private void processIDs(final List<String> out) {
    // class id
    for (final Entry<ClassID, ClassMethodInfo> id : classContext.getAllRegisteredClasses()) {
      out.add(LabelAndFrameUtils.makeLabelForClassID(id.getKey()) + ": EQU " + id.getValue().getUID());
    }

    // method id
    for (final Entry<MethodID, ClassMethodInfo> id : methodContext.getMethods()) {
      out.add(LabelAndFrameUtils.makeLabelForMethodID(id.getKey()) + ": EQU " + id.getValue().getUID());
    }
  }

  private void processClassFields(final List<String> out) {
    for (final ClassGen currentClass : workingClassPath.getAllClasses().values()) {
      final List<Field> fieldList = ClassUtils.findAllFields(workingClassPath, currentClass);
      final String className = currentClass.getClassName();
      int offset = 0;
      for (final Field f : fieldList) {
        out.add(LabelAndFrameUtils.makeLabelNameForFieldOffset(className, f.getName(), f.getType()) + ": EQU " + offset);
        offset += 2;
      }

      // reservation cells for static fields
      for (final Field f : currentClass.getFields()) {
        if (f.isStatic()) {
          out.add(LabelAndFrameUtils.makeLabelNameForField(className, f.getName(), f.getType()) + ": DEFW 0");
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
      out.addAll(Arrays.asList(text));
    }
  }

  private List<String> makeClassSizeArray() {
    final List<String> result = new ArrayList<>();
    for (final ClassGen curClass : workingClassPath.getAllClasses().values()) {
      if (curClass.isAbstract() || curClass.isInterface()) {
        continue;
      }
      result.add(LabelAndFrameUtils.makeLabelForClassSizeInfo(curClass.getClassName()) + ": EQU " + ClassUtils.calculateNeededAreaForClassInstance(workingClassPath, curClass));
    }

    return result;
  }

  private void processJNIClasses(final List<String> text) throws IOException {
    getLogger().logInfo("----PROCESS JNI CLASSES----");
    getLogger().logInfo("Detected " + classContext.getClassesWithDetectedJNI().size() + " class(es) contain(s) JNI methods");
    final NativeClassProcessor processor = new NativeClassProcessor(this);
    for (final ClassID classInfo : classContext.getClassesWithDetectedJNI()) {
      getLogger().logInfo("Process " + classInfo);
      text.addAll(Arrays.asList(processor.findNativeSources(classContext.findClassInfoForID(classInfo))));
    }
  }

  private void processBinaryData(final List<String> text) {
    getLogger().logInfo("----PROCESS BINARY DATA----");

    text.add("");
    text.add("; Included binary resources section");
    text.add("; -------------------------------------");

    for (final Entry<String, byte[]> binaryData : workingClassPath.getAllBinaryResources().entrySet()) {
      final String path = binaryData.getKey();

      // check for exclusion
      if (excludeResourcePatterns != null && excludeResourcePatterns.length > 0) {
        String matchedPattern = null;
        for (final String pattern : excludeResourcePatterns) {
          if (Utils.checkPathForAntPattern(path, pattern)) {
            matchedPattern = pattern;
            break;
          }
        }

        if (matchedPattern != null) {
          getLogger().logWarning("Excluded " + path + " for " + matchedPattern + " pattern");
          continue;
        }
      }

      final byte[] data = binaryData.getValue();

      if (data.length > 0xFFFF) {
        throw new IllegalArgumentException("Too big binary resource has been detected [" + path + "], it's length must not be greater than 64Kb.");
      }

      final String label = LabelAndFrameUtils.makeLabelForBinaryResource(path);

      getLogger().logInfo("Added the binary resource " + path + " as " + label);

      final String[] asmtext = Utils.byteArrayToAsm(label + ": ; binary resource " + path, data, -1);
      text.addAll(Arrays.asList(asmtext));
    }
    text.add("; -------------------------------------");
    text.add("");
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
      text.addAll(Arrays.asList(Utils.breakToLines(preprocessAdditionAssemblerText(addition, assemblerText))));
    }

    if (needMemoryManager) {
      getLogger().logInfo("Detected addition usage " + NeedsMemoryManager.class.getSimpleName());
      final String assemblerText = Utils.readTextResource(AbstractJvmCommandProcessor.class, (NeedsMemoryManager.class.getAnnotation(AdditionPath.class)).value());
      text.addAll(Arrays.asList(Utils.breakToLines(preprocessAdditionAssemblerText(NeedsMemoryManager.class, assemblerText))));
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
    if (mainClassName == null) {
      getLogger().logInfo("Use the first meet main-class");
    } else {
      getLogger().logInfo("Use the " + mainClassName + " as the main class");
    }

    if (workingClassPath.findMainClass(mainClassName, Z80_MAIN_METHOD_NAME, Z80_MAIN_METHOD_SIGNATURE) == null) {
      getLogger().logError("Can't find the main class");
      throw new IllegalStateException("Can't find the main class");
    }

    final ClassGen mainClass = workingClassPath.getMainClass();
    final Method mainMethod = workingClassPath.getMainMethod();

    return new MethodID(mainClass, mainMethod);
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
  public void registerConstantPoolItem(String constantLabel, Constant item) {
    this.classPoolConstants.put(constantLabel, item);
  }

  @Override
  public void registerCalledBootClassProcesser(AbstractBootClass bootClass) {
    Assertions.assertNotNull("Boot class must not be null", bootClass);
    usedBootstrapClassese.add(bootClass);
  }

  @Override
  public TranslatorLogger getLogger() {
    return messageLogger;
  }

  @Override
  public byte[] loadResourceForPath(final String path) {
    return workingClassPath.findNonClassForPath(path);
  }

  @Override
  public ClassContext getClassContext() {
    return classContext;
  }

  @Override
  public MethodContext getMethodContext() {
    return methodContext;
  }
}
