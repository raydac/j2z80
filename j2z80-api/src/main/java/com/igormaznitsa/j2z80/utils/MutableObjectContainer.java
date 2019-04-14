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
package com.igormaznitsa.j2z80.utils;

/**
 * A Special object-wrapper allows to change an object and emulate changing of method arguments inside th method
 *
 * @param <V> the type of the carried object
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class MutableObjectContainer<V> {
  private V value;

  public MutableObjectContainer() {
    this(null);
  }

  public MutableObjectContainer(final V value) {
    this.value = value;
  }

  public V get() {
    return this.value;
  }

  public void set(final V value) {
    this.value = value;
  }

  public boolean isNull() {
    return this.value == null;
  }
}
