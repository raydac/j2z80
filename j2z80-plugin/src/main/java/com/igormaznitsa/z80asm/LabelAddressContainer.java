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

package com.igormaznitsa.z80asm;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.j2z80.translator.utils.AsmAssertions;

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
    return this.labelMap.containsKey(labelName);
  }

  public int getLabelAddress(final String labelName) {
    return requireNonNull(this.labelMap.get(requireNonNull(labelName, "Name must not be null")),
        "Only exist label must be requested");
  }

  public void clear() {
    this.labelMap.clear();
  }

  public Set<Entry<String, Integer>> getSetOfRecords() {
    return labelMap.entrySet();
  }

  public boolean isEmpty() {
    return labelMap.isEmpty();
  }

  public void registerLabel(final String labelName, final int address) {
    requireNonNull(labelName, "Must not be null");
    AsmAssertions.assertAddress(address);
    final Integer addressAsInteger = address;
    if (this.flagAllowReplace) {
      this.labelMap.put(labelName, addressAsInteger);
    } else {
      if (this.labelMap.containsKey(labelName)) {
        throw new IllegalArgumentException("Label must not be defined already [" + labelName + ']');
      }
      this.labelMap.put(labelName, addressAsInteger);
    }
  }
}
