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
package com.igormaznitsa.z80asm;

import com.igormaznitsa.j2z80.utils.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The class implements a label data container.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class LabelAddressContainer {
  private final Map<String, Integer> labelMap = new LinkedHashMap<>();
  private boolean flagAllowReplace;

  public LabelAddressContainer() {
    this(false);
  }

  public LabelAddressContainer(final boolean allowReplaceRecords) {
    flagAllowReplace = allowReplaceRecords;
  }

  public boolean isReplaceAllowed() {
    return flagAllowReplace;
  }

  public void setReplaceAllowed(final boolean value) {
    this.flagAllowReplace = value;
  }

  public boolean hasLabel(final String labelName) {
    return labelMap.containsKey(labelName);
  }

  public int getLabelAddress(final String labelName) {
    Assert.assertNotNull("Name must not be null", labelName);
    final Integer address = labelMap.get(labelName);
    Assert.assertNotNull("Only exist label must be requested", address);

    return address;
  }

  public void clear() {
    labelMap.clear();
  }

  public Set<Entry<String, Integer>> getSetOfRecords() {
    return labelMap.entrySet();
  }

  public boolean isEmpty() {
    return labelMap.isEmpty();
  }

  public void registerLabel(final String labelName, final int address) {
    Assert.assertNotNull("Must not be null", labelName);
    Assert.assertAddress(address);

    final Integer addressAsInteger = address;

    if (flagAllowReplace) {
      labelMap.put(labelName, addressAsInteger);
    } else {
      Assert.assertFalse("Label must not be defined already [" + labelName + ']', labelMap.containsKey(labelName));
      labelMap.put(labelName, addressAsInteger);
    }
  }
}
