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

import static com.igormaznitsa.j2z80.translator.optimizator.OptimizationChainFactory.getOptimizators;
import static com.igormaznitsa.j2z80.translator.utils.AsmAssertions.assertAddress;
import static com.igormaznitsa.j2z80.translator.utils.ClassUtils.calculateInstanceSize;
import static com.igormaznitsa.j2z80.translator.utils.MethodUtils.isStaticInitializer;
import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelForBinaryResource;
import static com.igormaznitsa.j2z80.utils.LabelAndFrameUtils.makeLabelForClassSizeInfo;
import static com.igormaznitsa.j2z80.utils.Utils.byteArrayToAsm;
import static com.igormaznitsa.j2z80.utils.Utils.concatStringArrays;
import static com.igormaznitsa.j2z80.utils.Utils.intToString;
import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import com.igormaznitsa.j2z80.ClassContext;
import com.igormaznitsa.j2z80.MethodContext;
import com.igormaznitsa.j2z80.TranslatorContext;
import com.igormaznitsa.j2z80.TranslatorLogger;
import com.igormaznitsa.j2z80.api.additional.J2Z80AdditionPath;
import com.igormaznitsa.j2z80.api.additional.J2ZAdditionalBlock;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEINTERFACEManager;
import com.igormaznitsa.j2z80.api.additional.NeedsINVOKEVIRTUALManager;
import com.igormaznitsa.j2z80.api.additional.NeedsInstanceofManager;
import com.igormaznitsa.j2z80.api.additional.NeedsMemoryManager;
import com.igormaznitsa.j2z80.bootstrap.AbstractBootstrapClass;
import com.igormaznitsa.j2z80.ids.ClassID;
import com.igormaznitsa.j2z80.ids.ClassMethodInfo;
import com.igormaznitsa.j2z80.ids.MethodID;
import com.igormaznitsa.j2z80.jvmprocessors.AbstractJvmCommandProcessor;
import com.igormaznitsa.j2z80.jvmprocessors.Processor_INVOKESTATIC;
import com.igormaznitsa.j2z80.translator.jar.ZClassPath;
import com.igormaznitsa.j2z80.translator.jar.ZParsedJar;
import com.igormaznitsa.j2z80.translator.optimizator.AsmOptimizerChain;
import com.igormaznitsa.j2z80.translator.optimizator.OptimizationLevel;
import com.igormaznitsa.j2z80.translator.utils.ClassUtils;
import com.igormaznitsa.j2z80.utils.LabelAndFrameUtils;
import com.igormaznitsa.j2z80.utils.Utils;
import com.igormaznitsa.z80asm.asmcommands.ParsedAsmLine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;

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
  private final Set<MethodID> methodsUsedInInvokeInterface = new HashSet<>();
  private final Map<ClassMethodInfo, String[]> asmForMethods = new LinkedHashMap<>();
  private final Set<Class<? extends J2ZAdditionalBlock>> registeredAdditions = new HashSet<>();
  private final Set<AbstractBootstrapClass> bootstrapClasses = new HashSet<>();
  private final Map<String, Constant> classPoolConstants = new HashMap<>();
  private final Set<ClassID> classesForCheckCast = new HashSet<>();
  private String[] excludeResourcePatterns;
  private final OptimizationLevel optimizationLevel;

  public TranslatorImpl(final TranslatorLogger logger, final OptimizationLevel optimization,
                        final List<Path> jarArchives) {
    this.optimizationLevel = optimization;
    this.messageLogger = logger == null ? new DefaultTranslatorLogger() : logger;

    this.workingClassPath = new ZClassPath(this,
        jarArchives.stream().map(ZParsedJar::new).collect(Collectors.toList()));

    if (this.workingClassPath.getAllClasses().isEmpty()) {
      throw new IllegalStateException(
          "There is not any class in formed class path: " + this.workingClassPath);
    }
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

  public ClassGen findOverriddenMethodOnPath(final String className, final String superClassName,
                                             final MethodGen method) {
    ClassGen classGen = this.workingClassPath.findClassForName(className);
    while (classGen != null) {
      if (superClassName.equals(classGen.getClassName())) {
        return null;
      }
      for (final Method meth : classGen.getMethods()) {
        if (meth.getName().equals(method.getName()) &&
            meth.getReturnType().equals(method.getReturnType()) &&
            Arrays.deepEquals(meth.getArgumentTypes(), method.getArgumentTypes())) {
          return classGen;
        }
      }
      classGen = this.workingClassPath.findClassForName(classGen.getSuperclassName());
    }
    return null;
  }

  private void reset() {
    this.methodsUsedInInvokeInterface.clear();
    this.asmForMethods.clear();
    this.registeredAdditions.clear();
    this.bootstrapClasses.clear();
    this.classPoolConstants.clear();
    this.classesForCheckCast.clear();
  }

  @Override
  public void registerAdditionsUsedByClass(final Class<?> classToCheck) {
    registeredAdditions.addAll(ClassUtils.findAllAdditionalBlocksInClass(classToCheck));
  }

  @Override
  public List<String> translate(final String mainClassName, final int startAddress,
                                final int stackTop, final String[] patternsExcludeBinResources,
                                final ClassLoader bootstrapClassLoader)
      throws IOException {
    this.classContext.init();
    final List<MethodID> methodsToProcess =
        unmodifiableList(this.methodContext.findMethodsForProcessingInClassPath());

    this.reset();

    assertAddress(startAddress);
    assertAddress(stackTop);

    this.getLogger()
        .logInfo("Start address: " + intToString(startAddress));
    this.getLogger().logInfo("Stack top: " + intToString(stackTop));

    this.excludeResourcePatterns = patternsExcludeBinResources;
    final MethodID mainMethodID = this.findMainMethod(mainClassName);
    this.getLogger().logInfo(
        "Found main method: " + mainMethodID.getClassName() + "#" + mainMethodID.getMethodName());

    final ClassMethodInfo mainMethod = this.methodContext.findMethodInfo(mainMethodID);
    final String[] mainMethodAsm = this.translateMethod(mainMethodID, bootstrapClassLoader);
    final MainPrefixPostfixGenerator prefixPostfixGenerator =
        new MainPrefixPostfixGenerator(mainMethod, startAddress, stackTop);
    final String[] staticInitBlocks = this.processStaticInitializingBlocks();

    this.asmForMethods.put(mainMethod,
        concatStringArrays(prefixPostfixGenerator.generatePrefix(), staticInitBlocks, mainMethodAsm,
            prefixPostfixGenerator.generatePostfix()));

    this.getLogger().logDebug("Methods to process");
    this.getLogger().logDebug("-----------------------");
    methodsToProcess.forEach(x -> this.getLogger().logDebug(x.toString()));
    this.getLogger().logDebug("-----------------------");

    for (final MethodID methodToProcess : methodsToProcess) {
      if (!mainMethodID.equals(methodToProcess)) {
        this.translateMethod(methodToProcess, bootstrapClassLoader);
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

    this.processBootstrapClasses(result);
    this.processClassFields(result);
    this.processConstantPool(result);
    this.processJniClasses(result);
    this.processIDs(result);
    this.processBinaryData(result);
    this.processAdditions(result);

    result.addAll(makeClassSizeArray());

    if (this.optimizationLevel != null && this.optimizationLevel != OptimizationLevel.NONE) {
      this.getLogger().logWarning(
          "Optimization level: " + this.optimizationLevel);

      final List<ParsedAsmLine> asmLines = asParsedLines(result);
      final AsmOptimizerChain chain = getOptimizators(this, optimizationLevel);
      final List<String> optimizedAsString = asStringLines(chain.processSources(asmLines));

      optimizedAsString.add(0,
          "; optimization level is '" + this.optimizationLevel + '\'');

      return optimizedAsString;
    } else {
      this.getLogger().logInfo("No optimization");

      return result;
    }
  }

  private String[] processStaticInitializingBlocks() {
    this.getLogger().logInfo("----PROCESS STATIC INITIALIZERS ----");
    final List<ClassID> classesContainStaticInitializing = new ArrayList<>();
    for (final Entry<ClassID, ClassMethodInfo> id : this.classContext.getAllFoundClasses()) {
      final ClassGen classGen = id.getValue().getClassInfo();
      for (final Method method : classGen.getMethods()) {
        if (isStaticInitializer(method)) {
          classesContainStaticInitializing.add(id.getKey());
        }
      }
    }

    this.getLogger().logInfo("Static initialization method has been detected in " +
        classesContainStaticInitializing.size() + " class(es)");

    if (classesContainStaticInitializing.isEmpty()) {
      return new String[0];
    }

    classesContainStaticInitializing.sort((arg0, arg1) -> {
      if (arg0.equals(arg1)) {
        return 0;
      }

      if (this.classContext.isAccessible(this.classContext.findClassForID(arg1),
          this.classContext.findClassForID(arg0).getClassName())) {
        return 1;
      } else {
        return -1;
      }
    });

    final Processor_INVOKESTATIC invokeStaticProc =
        (Processor_INVOKESTATIC) AbstractJvmCommandProcessor.findProcessor(INVOKESTATIC.class);

    String[] result = new String[] {";------ STATIC INITIALIZING BLOCK ------"};

    for (final ClassID id : classesContainStaticInitializing) {
      final ClassGen classGen = classContext.findClassForID(id);
      result =
          concatStringArrays(result, invokeStaticProc.generateCallForStaticInitalizer(classGen));
    }

    return result;
  }

  private void processIDs(final List<String> list) {
    // class id
    for (final Entry<ClassID, ClassMethodInfo> id : this.classContext.getAllFoundClasses()) {
      list.add(
          LabelAndFrameUtils.makeLabelForClassID(id.getKey()) + ": EQU " + id.getValue().getUID());
    }

    // method id
    for (final Entry<MethodID, ClassMethodInfo> id : this.methodContext.getMethods().entrySet()) {
      list.add(
          LabelAndFrameUtils.makeLabelForMethodID(id.getKey()) + ": EQU " + id.getValue().getUID());
    }
  }

  private void processClassFields(final List<String> list) {
    for (final ClassGen currentClass : this.workingClassPath.getAllClasses().values()) {
      final List<Field> fieldList = ClassUtils.findAllFields(this.workingClassPath, currentClass);
      final String className = currentClass.getClassName();
      int offset = 0;
      for (final Field f : fieldList) {
        list.add(
            LabelAndFrameUtils.makeLabelNameForFieldOffset(className, f.getName(), f.getType()) +
                ": EQU " + offset);
        offset += 2;
      }

      // reservation cells for static fields
      for (final Field f : currentClass.getFields()) {
        if (f.isStatic()) {
          list.add(LabelAndFrameUtils.makeLabelNameForField(className, f.getName(), f.getType()) +
              ": DEFW 0");
        }
      }
    }
  }

  private void processConstantPool(final List<String> out) {
    for (final Entry<String, Constant> c : this.classPoolConstants.entrySet()) {
      final String label = c.getKey();
      final Constant constant = c.getValue();
      if (constant instanceof ConstantInteger) {
        out.add(label + ": DEFW #" +
            Integer.toHexString(((ConstantInteger) constant).getBytes() & 0xFFFF)
                .toUpperCase(Locale.ENGLISH));
      } else if (constant instanceof ConstantUtf8) {
        final String str = ((ConstantUtf8) constant).getBytes();
        out.add(label + ": DEFB " + str.length());
        out.add("DEFM \"" + str + '\"');
      }
    }
  }

  private void processBootstrapClasses(final List<String> list) {
    for (final AbstractBootstrapClass processor : this.bootstrapClasses) {
      final String[] text = processor.getAdditionalText();
      list.addAll(asList(text));
    }
  }

  private List<String> makeClassSizeArray() {
    final List<String> result = new ArrayList<>();
    for (final ClassGen current : this.workingClassPath.getAllClasses().values()) {
      if (current.isAbstract() || current.isInterface()) {
        continue;
      }
      result.add(makeLabelForClassSizeInfo(current.getClassName()) + ": EQU " +
          calculateInstanceSize(this.workingClassPath, current));
    }

    return result;
  }

  private void processJniClasses(final List<String> text) throws IOException {
    this.getLogger().logInfo("----PROCESS JNI CLASSES----");
    this.getLogger().logInfo("Detected " + classContext.getClassesWithJni().size() +
        " class(es) contain(s) JNI methods");
    final NativeClassProcessor processor = new NativeClassProcessor(this);
    for (final ClassID classInfo : classContext.getClassesWithJni()) {
      this.getLogger().logInfo("Process " + classInfo);
      text.addAll(asList(processor.findNativeSources(classContext.findClassInfoForID(classInfo))));
    }
  }

  private void processBinaryData(final List<String> text) {
    this.getLogger().logInfo("----PROCESS BINARY DATA----");

    text.add("");
    text.add("; Included binary resources section");
    text.add("; -------------------------------------");

    for (final Entry<String, byte[]> binaryData : workingClassPath.getAllBinaryResources()
        .entrySet()) {
      final String path = binaryData.getKey();

      // check for exclusion
      if (this.excludeResourcePatterns != null && this.excludeResourcePatterns.length > 0) {
        String matchedPattern = null;
        for (final String pattern : this.excludeResourcePatterns) {
          if (Utils.checkPathForAntPattern(path, pattern)) {
            matchedPattern = pattern;
            break;
          }
        }

        if (matchedPattern != null) {
          this.getLogger().logWarning("Excluded " + path + " for " + matchedPattern + " pattern");
          continue;
        }
      }

      final byte[] data = binaryData.getValue();
      if (data.length > 0xFFFF) {
        throw new IllegalArgumentException(
            "Detected too long binary resource, expected less than 64 Kb: " + path);
      }

      final String label = makeLabelForBinaryResource(path);
      this.getLogger().logInfo("Added the binary resource " + path + " as " + label);
      final String[] assembler = byteArrayToAsm(label + ": ; binary resource " + path, data, -1);
      text.addAll(asList(assembler));
    }
    text.add("; -------------------------------------");
    text.add("");
  }

  private void processAdditions(final List<String> text) throws IOException {
    this.getLogger().logInfo("----PROCESS ADDITIONS----");
    boolean needMemoryManager = false;
    for (final Class<? extends J2ZAdditionalBlock> addition : registeredAdditions) {
      if (addition == NeedsMemoryManager.class) {
        // memory manager must be in the end of code
        needMemoryManager = true;
        continue;
      }

      this.getLogger().logInfo("Detected an addition: " + addition.getSimpleName());

      final J2Z80AdditionPath path = addition.getAnnotation(J2Z80AdditionPath.class);
      if (path == null) {
        throw new IllegalStateException(
            "Detected an addition without path [" + addition.getCanonicalName() + ']');
      }

      final String assemblerText =
          Utils.readTextResource(AbstractJvmCommandProcessor.class, path.value());
      text.addAll(
          asList(Utils.breakToLines(preprocessAdditionAssemblerText(addition, assemblerText))));
    }

    if (needMemoryManager) {
      this.getLogger()
          .logInfo("Detected an addition use: " + NeedsMemoryManager.class.getSimpleName());
      final String assemblerText = Utils.readTextResource(AbstractJvmCommandProcessor.class,
          (NeedsMemoryManager.class.getAnnotation(
              J2Z80AdditionPath.class)).value());
      text.addAll(asList(Utils.breakToLines(
          preprocessAdditionAssemblerText(NeedsMemoryManager.class, assemblerText))));
    }
  }

  private String preprocessAdditionAssemblerText(final Class<? extends J2ZAdditionalBlock> addition,
                                                 final String text) {
    String result = text;
    if (addition == NeedsINVOKEINTERFACEManager.class) {
      result = text.replace(NeedsINVOKEINTERFACEManager.MACROS_INVOKEINTERFACE_TABLE,
          this.prepareInvokeinterfaceMacrosContent());
    } else if (addition == NeedsINVOKEVIRTUALManager.class) {
      final InvokeVirtualTable table = new InvokeVirtualTable(this);
      result = text.replace(NeedsINVOKEVIRTUALManager.MACROS_INVOKEVIRTUAL_TABLE, table.toAsm());
    } else if (addition == NeedsInstanceofManager.class) {
      final InstanceofTable table = new InstanceofTable(this, classesForCheckCast);
      result = text.replace(NeedsInstanceofManager.MACRO_INSTANCEOFTABLE, table.toAsm());
    }

    return result;
  }

  private String prepareInvokeinterfaceMacrosContent() {
    final InvokeinterfaceTable table = new InvokeinterfaceTable(this, methodsUsedInInvokeInterface);
    return table.generateAsm();
  }

  private String[] translateMethod(final MethodID methodId, final ClassLoader bootstrapClassLoader)
      throws IOException {
    final ClassMethodInfo method = this.methodContext.findMethodInfo(methodId);
    this.getLogger()
        .logInfo("Translating method: " + methodId.getClassName() + '#' + methodId.getMethodName());

    String[] resultAsm = null;
    try {
      if (!method.isNative()) {
        resultAsm = new MethodTranslator(this, method).translate(bootstrapClassLoader);
        this.asmForMethods.put(method, resultAsm);
      }
    } catch (Exception ex) {
      getLogger().logError("Exception during " + methodId + " [" + ex + ']');
      throw new IOException("Can't translate " + methodId, ex);
    }
    return resultAsm;
  }

  private MethodID findMainMethod(final String mainClassName) {
    if (mainClassName == null) {
      this.getLogger().logWarning("Auto-search of the main class");
    } else {
      this.getLogger().logInfo("Find in the main class: " + mainClassName);
    }

    if (this.workingClassPath.findMainClass(mainClassName, Z80_MAIN_METHOD_NAME,
        Z80_MAIN_METHOD_SIGNATURE) == null) {
      this.getLogger().logError("Can't find the main class");
      throw new IllegalStateException(
          "Can't find the main class: " + (mainClassName == null ? "AUTO" : mainClassName));
    }

    final ClassGen mainClass = this.workingClassPath.getMainClass();
    final Method mainMethod = this.workingClassPath.getMainMethod();

    return new MethodID(mainClass, mainMethod);
  }

  @Override
  public Integer registerClassForCastCheck(final ClassID classId) {
    classesForCheckCast.add(classId);
    return classContext.findClassUID(classId);
  }

  @Override
  public Integer registerInterfaceMethodForINVOKEINTERFACE(final MethodID methodId) {
    this.methodsUsedInInvokeInterface.add(methodId);
    return methodContext.findMethodUID(methodId);
  }

  @Override
  public void registerConstantPoolItem(final String constantLabel, final Constant item) {
    this.classPoolConstants.put(constantLabel, item);
  }

  @Override
  public void registerCalledBootClassProcesser(final AbstractBootstrapClass bootClass) {
    assertNotNull("A bootstrap class must not be null", bootClass);
    this.bootstrapClasses.add(bootClass);
  }

  @Override
  public TranslatorLogger getLogger() {
    return this.messageLogger;
  }

  @Override
  public byte[] loadResourceForPath(final String path) {
    return this.workingClassPath.findNonClassForPath(path);
  }

  @Override
  public ClassContext getClassContext() {
    return this.classContext;
  }

  @Override
  public MethodContext getMethodContext() {
    return this.methodContext;
  }
}
