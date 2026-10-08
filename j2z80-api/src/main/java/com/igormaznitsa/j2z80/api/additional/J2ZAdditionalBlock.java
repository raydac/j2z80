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

package com.igormaznitsa.j2z80.api.additional;

/**
 * Marker interface for runtime support blocks that a translated Java class can declare to signal
 * that additional Z80 assembly fragments must be linked into the final program image.
 *
 * <p>Implementations are typically small marker interfaces such as
 * {@link NeedsMemoryManager} or {@link NeedsATHROWManager}. When the translator sees a class
 * implements one of these markers, it loads the corresponding assembly resource and binds the
 * runtime labels expected by the generated code.</p>
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public interface J2ZAdditionalBlock {

}
