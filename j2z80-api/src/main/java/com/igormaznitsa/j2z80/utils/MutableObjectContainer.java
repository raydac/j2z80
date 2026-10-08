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

/**
 * Mutable holder for an object reference that can be changed after construction. This utility is
 * mainly used by translator internals that need to emulate by-reference argument updates without
 * creating a dedicated wrapper type for each call site.
 *
 * @param <V> the type stored in the container
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MutableObjectContainer<V> {
  private V value;

  /**
   * Creates an empty container whose current value is {@code null}.
   */
  public MutableObjectContainer() {
    this(null);
  }

  /**
   * Creates a container with an initial value.
   *
   * @param value the initial value, or {@code null} for an empty container
   */
  public MutableObjectContainer(final V value) {
    this.value = value;
  }

  /**
   * Returns the current value stored in the container.
   *
   * @return the current value, or {@code null}
   */
  public V get() {
    return this.value;
  }

  /**
   * Replaces the stored value.
   *
   * @param value the new value, or {@code null}
   */
  public void set(final V value) {
    this.value = value;
  }

  /**
   * Checks whether the container is currently empty.
   *
   * @return {@code true} when the stored value is {@code null}
   */
  public boolean isNull() {
    return this.value == null;
  }
}
