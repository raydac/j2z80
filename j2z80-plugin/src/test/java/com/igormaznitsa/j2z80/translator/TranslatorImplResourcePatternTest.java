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

package com.igormaznitsa.j2z80.translator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TranslatorImplResourcePatternTest {

  @Test
  public void matchesAntPatternsAgainstRootedResourcePathsCaseInsensitively() {
    assertTrue(TranslatorImpl.matchesResourcePattern("hello/world.xml", "/**/*.xml"));
    assertTrue(TranslatorImpl.matchesResourcePattern("/hello/some/world.xml", "/**/*.xml"));
    assertTrue(TranslatorImpl.matchesResourcePattern("test.res", "/**/test.res"));
    assertTrue(TranslatorImpl.matchesResourcePattern("test.ReS", "/**/test.res"));
    assertFalse(TranslatorImpl.matchesResourcePattern("hello/world.properties", "/**/*.xml"));
  }
}
