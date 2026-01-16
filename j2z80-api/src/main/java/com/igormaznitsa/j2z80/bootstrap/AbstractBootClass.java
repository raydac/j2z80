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
package com.igormaznitsa.j2z80.bootstrap;

import com.igormaznitsa.j2z80.TranslatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.generic.Type;

/**
 * The class is the parent for all bootstrap classes used by the translator, it automates their search and processing.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractBootClass {

  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  private static final Map<String, AbstractBootClass> insideCache = new HashMap<String, AbstractBootClass>();

  public static AbstractBootClass findProcessor(final String className) {
    AbstractBootClass result = insideCache.get(className);
    if (result == null) {
      final String newName = AbstractBootClass.class.getPackage().getName() + "." + className;
      try {
        final Class<? extends AbstractBootClass> japiClass =
            Class.forName(newName).asSubclass(AbstractBootClass.class);
        result = japiClass.getDeclaredConstructor().newInstance();
        insideCache.put(className, result);
      } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException ex) {
        // ignore
      } catch (IllegalAccessException ex) {
        throw new RuntimeException("Can't get access to a bootstrap class: " + className, ex);
      } catch (InstantiationException ex) {
        throw new RuntimeException("Can't instantiate a bootstrap class: " + className, ex);
      }
    }
    return result;
  }

  /**
   * Extract the emulated class name of the class
   *
   * @return the class name part of the emulated bootstrap class
   */
  protected String extractEmulatedJavaClassName() {
    return this.getClass().getCanonicalName().substring(AbstractBootClass.class.getPackage().getName().length() + 1);
  }

  /**
   * An Auxiliary method to throw a boot class exception for a method
   *
   * @param methodName the method name, must not be null
   * @param result     the method result signature, must not be null
   * @param args       the method argument signatures, must not be null
   */
  public void throwBootClassExceptionForMethod(final String methodName, final Type result, final Type[] args) {
    final String className = extractEmulatedJavaClassName();
    final String methodSignature = Type.getMethodSignature(result, args);
    throw new BootClassException(
        "Unsupported method: " + className + '.' + methodName + " " + methodSignature, className,
        methodName, methodSignature);
  }

  /**
   * An Auxiliary method to throw a boot class exception for a field
   *
   * @param fieldName the field name, must not be null
   * @param type      the field type signature, must not be null
   */
  public void throwBootClassExceptionForField(final String fieldName, final Type type) {
    final String className = extractEmulatedJavaClassName();
    throw new BootClassException(
        "Unsupported field: " + className + '.' + fieldName + " " + type.toString(), className,
        fieldName, type.getSignature());
  }

  /**
   * It allows to figure out that a method needs a stack frame when it is invoked
   *
   * @param context         the translator context, must not be null
   * @param methodName      the method name, must not be null
   * @param methodArguments the method argument signatures, must not be null
   * @param resultType      the method result type signature, must not be null
   * @return it returns true if the method needs a stack frame for its work
   */
  public abstract boolean doesInvokeNeedFrame(TranslatorContext context, String methodName, Type[] methodArguments, Type resultType);

  /**
   * Generate method invocation code
   *
   * @param context         the translator context, must not be null
   * @param methodName      the method name, must not be null
   * @param methodArguments the method argument signatures, must not be null
   * @param resultType      the method result signature, must not be null
   * @return a string array contains assembler commands to invoke the method
   */
  public abstract String[] generateInvocation(TranslatorContext context, String methodName, Type[] methodArguments, Type resultType);

  /**
   * Generate a field getter
   *
   * @param context   the translator context, must not be null
   * @param fieldName the field name, must not be null
   * @param fieldType the field type signature, must not be null
   * @param isStatic  the flag shows that the field is static if the flag is true
   * @return a string array contains assembler commands to read the field
   */
  public abstract String[] generateFieldGetter(TranslatorContext context, String fieldName, Type fieldType, boolean isStatic);

  /**
   * Generate a field setter
   *
   * @param context   the translator context, must not be null
   * @param fieldName the field name, must not be null
   * @param fieldType the field type signature, must not be null
   * @param isStatic  the flag shows that the field is a static one if the flag is true
   * @return a string array contains assembler commands to set the field
   */
  public abstract String[] generateFieldSetter(TranslatorContext context, String fieldName, Type fieldType, boolean isStatic);

  /**
   * The method is called once after translation and a boot class can add some assembler text in the special section if it needs
   *
   * @return an array contains assembler text
   */
  public String[] getAdditionalText() {
    return EMPTY_STRING_ARRAY;
  }
}
