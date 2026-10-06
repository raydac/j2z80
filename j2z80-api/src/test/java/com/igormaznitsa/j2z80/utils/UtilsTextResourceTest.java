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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import org.junit.Test;

public class UtilsTextResourceTest {

  @Test
  public void readsTextResourceAsUtf8AndNormalizesLineEndings() throws Exception {
    assertEquals("first\ncaf\u00e9\n",
        Utils.readTextResource(UtilsTextResourceTest.class, "text-resource.txt"));
  }

  @Test
  public void throwsWhenTextResourceIsMissing() throws Exception {
    try {
      Utils.readTextResource(UtilsTextResourceTest.class, "missing-text-resource.txt");
      fail("Expected FileNotFoundException");
    } catch (FileNotFoundException expected) {
      assertEquals("Can't find resource missing-text-resource.txt", expected.getMessage());
    }
  }
}
