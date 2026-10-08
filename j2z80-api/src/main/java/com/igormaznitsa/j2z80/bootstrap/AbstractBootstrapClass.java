/*
 * Copyright 2012-2026 Igor Maznitsa.
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bcel.generic.Type;

/**
 * Base class for all bootstrap definitions that emulate a Java runtime type during translation.
 * Bootstrap processors are responsible for translating calls and field access to a limited subset
 * of JVM semantics into the Z80 assembly fragments needed by the generated program.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public abstract class AbstractBootstrapClass {
  public static final String J2Z80_BOOTSTRAP_PACKAGE_PREFIX = "j2z80.bootstrap";

  private static final Map<String, AbstractBootstrapClass> internalCache =
      new ConcurrentHashMap<>();

  /**
   * Finds or creates a bootstrap processor for a Java class name.
   *
   * @param className   the Java class name to resolve, must not be {@code null}
   * @param classLoader the class loader used to load bootstrap implementations, must not be {@code null}
   * @return the processor instance for the class, or {@code null} if none is registered
   */
  public static AbstractBootstrapClass findProcessor(final String className,
                                                     final ClassLoader classLoader) {
    AbstractBootstrapClass result = internalCache.get(className);
    if (result == null) {
      final String normalizedName;
      if (className.startsWith("java.") || className.startsWith("javax.")) {
        normalizedName = J2Z80_BOOTSTRAP_PACKAGE_PREFIX + '.' + className;
      } else {
        normalizedName = className;
      }
      try {
        final Class<? extends AbstractBootstrapClass> japiClass =
            classLoader.loadClass(normalizedName).asSubclass(AbstractBootstrapClass.class);
        result = japiClass.getDeclaredConstructor().newInstance();
        internalCache.put(className, result);
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
   * Extracts the Java class name represented by this bootstrap implementation.
   *
   * @return the emulated Java class name without the shared bootstrap package prefix
   */
  protected String extractEmulatedJavaClassName() {
    return this.getClass().getCanonicalName().substring(
        AbstractBootstrapClass.class.getPackage().getName().length() + 1);
  }

  /**
   * Throws a standardized exception for an unsupported method request.
   *
   * @param methodName the method name, must not be {@code null}
   * @param result the result type descriptor, must not be {@code null}
   * @param args the argument type descriptors, must not be {@code null}
   */
  public void throwBootClassExceptionForMethod(final String methodName, final Type result,
                                               final Type[] args) {
    final String className = extractEmulatedJavaClassName();
    final String methodSignature = Type.getMethodSignature(result, args);
    throw new BootClassException(
        "Unsupported method: " + className + '.' + methodName + " " + methodSignature, className,
        methodName, methodSignature);
  }

  /**
   * Throws a standardized exception for an unsupported field request.
   *
   * @param fieldName the field name, must not be {@code null}
   * @param type the field type descriptor, must not be {@code null}
   */
  public void throwBootClassExceptionForField(final String fieldName, final Type type) {
    final String className = extractEmulatedJavaClassName();
    throw new BootClassException(
        "Unsupported field: " + className + '.' + fieldName + " " + type.toString(), className,
        fieldName, type.getSignature());
  }

  /**
   * Determines whether the given method invocation requires a dedicated stack frame.
   *
   * @param context the translator context in which the invocation takes place, must not be {@code null}
   * @param methodName the method name, must not be {@code null}
   * @param methodArguments the argument type descriptors, must not be {@code null}
   * @param resultType the return type descriptor, must not be {@code null}
   * @return {@code true} if the invocation needs frame setup before execution, otherwise {@code false}
   */
  public abstract boolean doesInvokeNeedFrame(TranslatorContext context, String methodName,
                                              Type[] methodArguments, Type resultType);

  /**
   * Generates the assembly instructions needed to execute a method call represented by this
   * bootstrap class.
   *
   * @param context the translator context in which the invocation is generated, must not be {@code null}
   * @param methodName the method name, must not be {@code null}
   * @param methodArguments the argument type descriptors, must not be {@code null}
   * @param resultType the return type descriptor, must not be {@code null}
   * @return a list of Z80 assembly instructions that implement the method call
   */
  public abstract List<String> generateInvocation(TranslatorContext context, String methodName,
                                                  Type[] methodArguments, Type resultType);

  /**
   * Generates assembly instructions that read a field value from this bootstrap type.
   *
   * @param context the translator context in which the field access is generated, must not be {@code null}
   * @param fieldName the field name, must not be {@code null}
   * @param fieldType the field type descriptor, must not be {@code null}
   * @param isStatic {@code true} when the field is static
   * @return the generated field-get assembly instructions
   */
  public abstract List<String> generateFieldGetter(TranslatorContext context, String fieldName,
                                               Type fieldType, boolean isStatic);

  /**
   * Generates assembly instructions that write a field value for this bootstrap type.
   *
   * @param context the translator context in which the field assignment is generated, must not be {@code null}
   * @param fieldName the field name, must not be {@code null}
   * @param fieldType the field type descriptor, must not be {@code null}
   * @param isStatic {@code true} when the field is static
   * @return the generated field-set assembly instructions
   */
  public abstract List<String> generateFieldSetter(TranslatorContext context, String fieldName,
                                               Type fieldType, boolean isStatic);

  /**
   * Allows a bootstrap implementation to append additional assembly fragments after the normal
   * translation has completed.
   *
   * @return the additional assembly lines to append, or an empty list when no fragments are needed
   */
  public List<String> getAdditionalText() {
    return List.of();
  }
}
