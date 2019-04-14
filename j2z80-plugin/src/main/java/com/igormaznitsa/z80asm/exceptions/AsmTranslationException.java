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
package com.igormaznitsa.z80asm.exceptions;

/**
 * It is the special exception which will be thrown if there is some problems in code translation.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@SuppressWarnings("serial")
public class AsmTranslationException extends RuntimeException {
  private final String srcString;
  private final int lineNumber;

  public AsmTranslationException(final String message, final String srcString, final int lineNumber, final Throwable cause) {
    super(message, cause);
    this.srcString = srcString;
    this.lineNumber = lineNumber;
  }

  public String getSrcString() {
    return srcString;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public String toString() {
    return getMessage() + "\'" + getSrcString() + "\' at " + getLineNumber() + " line";
  }
}
