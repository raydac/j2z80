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

package com.igormaznitsa.j2z80.utils;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for common string, collection, and resource handling tasks used throughout the
 * translator and its runtime support. The class is intentionally static and does not maintain any
 * mutable state.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public final class Utils {
  private Utils() {

  }

  /**
   * Silently close any closeable object
   *
   * @param closeableOne the object to be closed, it can be null
   */
  public static void silentlyClose(final Closeable closeableOne) {
    if (closeableOne != null) {
      try {
        closeableOne.close();
      } catch (IOException ex) {
        // ignored exception
      }
    }
  }

  /**
   * Splits text into lines.
   *
   * @param text the text to split, must not be {@code null}
   * @return an immutable list containing one element for each line
   */
  public static List<String> breakLines(final String text) {
    return List.of(StringUtils.split(text, '\n'));
  }

  /**
   * Read a text (UTF-8 encoded) resource in a class path of a class
   *
   * @param thisClass the class to be used to get a class path, must not be null
   * @param resource  the resource name, must not be null
   * @return the loaded text as a solid string
   * @throws IOException           it will be thrown if there is any transport error
   * @throws FileNotFoundException it will be thrown if the resource is not found
   */
  public static String readTextResource(final Class<?> thisClass, final String resource)
      throws IOException {
    final InputStream resourceStream = thisClass.getResourceAsStream(resource);
    if (resourceStream == null) {
      throw new FileNotFoundException("Can't find resource " + resource);
    }
    try (InputStream input = resourceStream) {
      final List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8);
      return lines.isEmpty() ? "" : String.join("\n", lines) + '\n';
    }
  }

  /**
   * Concatenates string lists in argument order.
   *
   * @param lists the string lists to concatenate, must not be {@code null}
   * @return a list containing all elements of the argument lists
   */
  @SafeVarargs
  public static List<String> concatStringLists(final List<String>... lists) {
    final List<String> result = new ArrayList<>();
    for (final List<String> list : lists) {
      result.addAll(list);
    }
    return result;
  }

  /**
   * Convert an integer into an ASM HEX representation
   *
   * @param value an integer value to be converted
   * @return a hex string representation of the integer
   */
  public static String intToString(final int value) {
    return value + "(#" + Integer.toHexString(value).toUpperCase() + ')';
  }

  /**
   * Convert a long into an ASM HEX representation
   *
   * @param value a long value to be converted
   * @return a hex string representation of the long
   */
  public static String longToString(final long value) {
    return value + "(#" + Long.toHexString(value).toUpperCase() + ')';
  }

  /**
   * Convert a byte array into asm compatible representation (DEFB) with limit for values per line
   *
   * @param firstLine            the first line for the result text block, it can be null
   * @param array                a byte array to be converted, must not be null
   * @param maxValueItemsPerLine the number of values allowed per a line, if -1 then it will be default value
   * @return a list of assembly source lines representing the converted byte data
   */
  public static List<String> byteArrayToAsm(
      final String firstLine,
      final byte[] array,
      final int maxValueItemsPerLine
  ) {
    final StringBuilder buffer = new StringBuilder(firstLine == null ? "" : firstLine);

    if (firstLine != null
        && !buffer.isEmpty()
        && buffer.charAt(buffer.length() - 1) != '\n'
    ) {
      buffer.append('\n');
    }

    final int maxPerString = maxValueItemsPerLine <= 0 ? 32 : maxValueItemsPerLine;
    int len = array.length;
    int index = 0;
    while (len > 0) {
      int stringIntemCounter = 0;
      buffer.append("DEFB ");
      while (len > 0 && stringIntemCounter < maxPerString) {
        if (stringIntemCounter > 0) {
          buffer.append(',');
        }
        buffer.append('#')
            .append(Integer.toHexString(array[index++] & 0xFF).toUpperCase(Locale.ENGLISH));

        stringIntemCounter++;
        len--;
      }
      buffer.append('\n');
    }

    return breakLines(buffer.toString());
  }

}
